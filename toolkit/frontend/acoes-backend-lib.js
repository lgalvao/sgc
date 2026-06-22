import path from "node:path";
import fs from "fs-extra";
import {globby} from "globby";
import {parse} from "@vue/compiler-sfc";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";

const PREFIXO_ACAO = /^(pode|habilitar|mostrar|exibir|ocultar|desabilitar|permitir)[A-Z]/;
const NOMES_UI_LOCAL = /(Modal|Popover|Tooltip|Detalhes|Preview|Filtro|Filtros|Busca|Resultado|Calendario|Dropdown|Menu|Toast|Alerta|Aba|Painel|Bloco)/;
const SINAIS_DOMINIO = /(perfilSelecionado|Perfil\.|situacao[A-Z\w]*|situacao\.|tipoProcesso|TipoProcesso|CONSENSO_|DIAGNOSTICO_|AUTOAVALIACAO_|CADASTRO_|HOMOLOGA|ADMIN|GESTOR|CHEFE|SERVIDOR)/;
const SINAIS_COLECAO = /\.(some|every|filter|find)\s*\(/;
const SINAIS_COMPOSICAO = /(&&|\|\||\?|===|!==|==|!=| if\s*\()/;
const SINAL_FLAG_BACKEND = /(?:permissoes|permissao|query\.data|props|subprocesso|contexto|acaoPrincipalMapa|acoesServidor)[\w.?()[\]'"]*\.(pode|habilitar|mostrar|exibir)[A-Z]\w*/;
const CAMINHOS_IGNORADOS = [
    "**/*.spec.ts",
    "**/*.test.ts",
    "**/__tests__/**",
    "**/mocks/**",
    "**/fixtures/**",
];

function normalizarCaminho(caminho) {
    return caminho.replaceAll("\\", "/");
}

function contarLinha(conteudo, indice) {
    return conteudo.slice(0, indice).split("\n").length;
}

async function listarArquivosFrontend(diretorioFrontend) {
    return globby(["src/**/*.{ts,vue}"], {
        cwd: diretorioFrontend,
        absolute: true,
        gitignore: true,
        ignore: CAMINHOS_IGNORADOS,
    });
}

function extrairConteudoAnalise(caminhoArquivo, conteudo) {
    if (!caminhoArquivo.endsWith(".vue")) {
        return conteudo;
    }

    const {descriptor} = parse(conteudo, {filename: caminhoArquivo});
    return [
        descriptor.script?.content ?? "",
        descriptor.scriptSetup?.content ?? "",
    ].filter(Boolean).join("\n");
}

function lerWaivers(caminhoWaivers) {
    if (!fs.existsSync(caminhoWaivers)) {
        return new Set();
    }

    const conteudo = fs.readJsonSync(caminhoWaivers);
    return new Set((conteudo.waivers ?? []).map((item) => chaveViolacao(item)));
}

function chaveViolacao(item) {
    return `${normalizarCaminho(item.arquivo)}::${item.identificador}::${item.regra}`;
}

function simplificarExpressao(expressao) {
    return expressao
        .replaceAll(/\s+/g, " ")
        .replaceAll(/;$/g, "")
        .trim();
}

function normalizarExpressaoComputed(expressao) {
    const limpa = simplificarExpressao(expressao);
    const matchComputed = limpa.match(/^computed\s*\(\s*(?:\(\s*\)\s*=>|function\s*\(\s*\)\s*\{?\s*return)\s*(?<corpo>[\s\S]*?)\s*\)?$/);
    if (!matchComputed?.groups?.corpo) {
        return limpa;
    }
    return simplificarExpressao(matchComputed.groups.corpo.replaceAll(/}\s*$/g, ""));
}

function ehPassagemDiretaBackend(expressao) {
    const corpo = normalizarExpressaoComputed(expressao)
        .replace(/^Boolean\s*\((.*)\)$/u, "$1")
        .replace(/^!!/u, "")
        .replace(/===\s*true$/u, "")
        .replace(/!==\s*false$/u, "")
        .replace(/\?\?\s*false$/u, "")
        .replace(/\?\./gu, ".")
        .trim();
    const corpoSemOperadoresSeguros = corpo
        .replace(/\?\?\s*false/gu, "")
        .replace(/\?\./gu, ".")
        .trim();

    return SINAL_FLAG_BACKEND.test(corpoSemOperadoresSeguros)
        && !SINAIS_DOMINIO.test(corpoSemOperadoresSeguros)
        && !SINAIS_COLECAO.test(corpoSemOperadoresSeguros)
        && !/[&|?:]/u.test(corpoSemOperadoresSeguros);
}

function classificarViolacao(expressao) {
    const corpo = normalizarExpressaoComputed(expressao);

    if (ehPassagemDiretaBackend(corpo)) {
        return null;
    }

    if (SINAIS_DOMINIO.test(corpo)) {
        return {
            regra: "frontend-sem-regra-local-acoes",
            motivo: "calculo de acao baseado em perfil, situacao ou enum de dominio",
        };
    }

    if (SINAL_FLAG_BACKEND.test(corpo) && SINAIS_COMPOSICAO.test(corpo)) {
        return {
            regra: "frontend-sem-regra-local-acoes",
            motivo: "composicao local de flags de permissao retornadas pelo backend",
        };
    }

    if (SINAIS_COLECAO.test(corpo) && /(permissoes|situacao|perfil)/i.test(corpo)) {
        return {
            regra: "frontend-sem-regra-local-acoes",
            motivo: "calculo de acao derivado de colecao com estado de dominio",
        };
    }

    return null;
}

function registrarViolacao(violacoes, contexto, ocorrencia) {
    if (!PREFIXO_ACAO.test(ocorrencia.identificador) || NOMES_UI_LOCAL.test(ocorrencia.identificador)) {
        return;
    }

    const classificacao = classificarViolacao(ocorrencia.expressao);
    if (!classificacao) {
        return;
    }

    const violacao = {
        arquivo: contexto.arquivoRelativo,
        linha: ocorrencia.linha,
        identificador: ocorrencia.identificador,
        regra: classificacao.regra,
        motivo: classificacao.motivo,
        trecho: simplificarExpressao(ocorrencia.expressao).slice(0, 180),
    };
    violacoes.push(violacao);
}

function encontrarDeclaracoes(conteudo) {
    const declaracoes = [];
    const regex = /\bconst\s+(?<identificador>[A-Za-z_$][\w$]*)\s*=\s*(?<expressao>computed\s*\([\s\S]*?\)\s*|[^;\n]+)\s*;/gu;
    let match = regex.exec(conteudo);

    while (match) {
        declaracoes.push({
            identificador: match.groups.identificador,
            expressao: match.groups.expressao,
            linha: contarLinha(conteudo, match.index),
        });
        match = regex.exec(conteudo);
    }

    return declaracoes;
}

function encontrarPropriedadesAcao(conteudo) {
    const propriedades = [];
    const regex = /(?<identificador>pode[A-Z]\w*|habilitar[A-Z]\w*|mostrar[A-Z]\w*|exibir[A-Z]\w*|ocultar[A-Z]\w*|desabilitar[A-Z]\w*|permitir[A-Z]\w*)\s*:\s*(?<expressao>[^,\n}]+)/gu;
    let match = regex.exec(conteudo);

    while (match) {
        propriedades.push({
            identificador: match.groups.identificador,
            expressao: match.groups.expressao,
            linha: contarLinha(conteudo, match.index),
        });
        match = regex.exec(conteudo);
    }

    return propriedades;
}

function auditarConteudo({conteudo, arquivoRelativo}) {
    const violacoes = [];
    const contexto = {arquivoRelativo};

    encontrarDeclaracoes(conteudo).forEach((ocorrencia) => registrarViolacao(violacoes, contexto, ocorrencia));
    encontrarPropriedadesAcao(conteudo).forEach((ocorrencia) => registrarViolacao(violacoes, contexto, ocorrencia));

    return violacoes;
}

async function auditarAcoesBackendFrontend(opcoes = {}) {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioFrontend = path.join(diretorioBase, "frontend");
    const caminhoWaivers = opcoes.waivers ?? path.join(
        diretorioBase,
        "toolkit",
        "qualidade",
        "frontend-arquitetura",
        "acoes-backend-waivers.json",
    );
    const waivers = lerWaivers(caminhoWaivers);
    const arquivos = await listarArquivosFrontend(diretorioFrontend);
    const violacoes = [];

    for (const caminhoArquivo of arquivos) {
        const conteudoOriginal = await fs.readFile(caminhoArquivo, "utf-8");
        const conteudo = extrairConteudoAnalise(caminhoArquivo, conteudoOriginal);
        const arquivoRelativo = normalizarCaminho(path.relative(diretorioBase, caminhoArquivo));
        violacoes.push(...auditarConteudo({conteudo, arquivoRelativo}));
    }

    const enriquecidas = violacoes.map((violacao) => ({
        ...violacao,
        dispensada: waivers.has(chaveViolacao(violacao)),
    }));

    return {
        regra: "frontend-sem-regra-local-acoes",
        caminhoWaivers: normalizarCaminho(path.relative(diretorioBase, caminhoWaivers)),
        total: enriquecidas.length,
        dispensadas: enriquecidas.filter((violacao) => violacao.dispensada).length,
        violacoes: enriquecidas.filter((violacao) => !violacao.dispensada),
        todasViolacoes: enriquecidas,
    };
}

export {
    auditarAcoesBackendFrontend,
};
