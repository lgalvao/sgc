import path from "node:path";
import {existsSync, readFileSync} from "node:fs";
import {readFile} from "node:fs/promises";
import {globby} from "globby";
import {parse} from "@vue/compiler-sfc";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

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

type RegraViolacao = "frontend-sem-regra-local-acoes";

interface OcorrenciaAcao {
    identificador: string;
    expressao: string;
    linha: number;
}

interface ContextoAuditoria {
    arquivoRelativo: string;
}

interface ClassificacaoViolacao {
    regra: RegraViolacao;
    motivo: string;
}

interface ViolacaoAcao {
    arquivo: string;
    linha: number;
    identificador: string;
    regra: RegraViolacao;
    motivo: string;
    trecho: string;
}

interface ViolacaoEnriquecida extends ViolacaoAcao {
    dispensada: boolean;
}

interface RegistroExcecao {
    arquivo: string;
    identificador: string;
    regra: string;
}

interface OpcoesAuditoriaAcoes {
    base?: string;
    excecoes?: string;
}

interface ResultadoAuditoriaAcoes {
    regra: RegraViolacao;
    caminhoExcecoes: string;
    total: number;
    dispensadas: number;
    violacoes: ViolacaoEnriquecida[];
    todasViolacoes: ViolacaoEnriquecida[];
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll("\\", "/");
}

function contarLinha(conteudo: string, indice: number): number {
    return conteudo.slice(0, indice).split("\n").length;
}

async function listarArquivosFrontend(diretorioFrontend: string): Promise<string[]> {
    return globby(["src/**/*.{ts,vue}"], {
        cwd: diretorioFrontend,
        absolute: true,
        gitignore: true,
        ignore: CAMINHOS_IGNORADOS,
    });
}

function extrairConteudoAnalise(caminhoArquivo: string, conteudo: string): string {
    if (!caminhoArquivo.endsWith(".vue")) {
        return conteudo;
    }

    const {descriptor} = parse(conteudo, {filename: caminhoArquivo});
    return [
        descriptor.script?.content ?? "",
        descriptor.scriptSetup?.content ?? "",
    ].filter(Boolean).join("\n");
}

function ehRegistroExcecao(valor: unknown): valor is RegistroExcecao {
    if (!valor || typeof valor !== "object") {
        return false;
    }

    const registro = valor as Record<string, unknown>;
    return typeof registro.arquivo === "string"
        && typeof registro.identificador === "string"
        && typeof registro.regra === "string";
}

function lerExcecoes(caminhoExcecoes: string): Set<string> {
    if (!existsSync(caminhoExcecoes)) {
        return new Set();
    }

    const conteudo: unknown = JSON.parse(readFileSync(caminhoExcecoes, "utf-8"));
    if (!conteudo || typeof conteudo !== "object") {
        return new Set();
    }

    const registros = (conteudo as {excecoes?: unknown}).excecoes;
    if (!Array.isArray(registros)) {
        return new Set();
    }

    return new Set(registros.filter(ehRegistroExcecao).map(chaveViolacao));
}

function chaveViolacao(item: Pick<RegistroExcecao, "arquivo" | "identificador" | "regra">): string {
    return `${normalizarCaminho(item.arquivo)}::${item.identificador}::${item.regra}`;
}

function simplificarExpressao(expressao: string): string {
    return expressao
        .replaceAll(/\s+/g, " ")
        .replaceAll(/;$/g, "")
        .trim();
}

function normalizarExpressaoComputed(expressao: string): string {
    const limpa = simplificarExpressao(expressao);
    const matchComputed = limpa.match(/^computed\s*\(\s*(?:\(\s*\)\s*=>|function\s*\(\s*\)\s*\{?\s*return)\s*(?<corpo>[\s\S]*?)\s*\)?$/);
    if (!matchComputed?.groups?.corpo) {
        return limpa;
    }
    return simplificarExpressao(matchComputed.groups.corpo.replaceAll(/}\s*$/g, ""));
}

function ehPassagemDiretaBackend(expressao: string): boolean {
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

function classificarViolacao(expressao: string): ClassificacaoViolacao | null {
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

function registrarViolacao(
    violacoes: ViolacaoAcao[],
    contexto: ContextoAuditoria,
    ocorrencia: OcorrenciaAcao
): void {
    if (!PREFIXO_ACAO.test(ocorrencia.identificador) || NOMES_UI_LOCAL.test(ocorrencia.identificador)) {
        return;
    }

    const classificacao = classificarViolacao(ocorrencia.expressao);
    if (!classificacao) {
        return;
    }

    violacoes.push({
        arquivo: contexto.arquivoRelativo,
        linha: ocorrencia.linha,
        identificador: ocorrencia.identificador,
        regra: classificacao.regra,
        motivo: classificacao.motivo,
        trecho: simplificarExpressao(ocorrencia.expressao).slice(0, 180),
    });
}

function encontrarDeclaracoes(conteudo: string): OcorrenciaAcao[] {
    const declaracoes: OcorrenciaAcao[] = [];
    const regex = /\bconst\s+(?<identificador>[A-Za-z_$][\w$]*)\s*=\s*(?<expressao>computed\s*\([\s\S]*?\)\s*|[^;\n]+)\s*;/gu;
    let match = regex.exec(conteudo);

    while (match) {
        const identificador = match.groups?.identificador;
        const expressao = match.groups?.expressao;
        if (identificador && expressao) {
            declaracoes.push({
                identificador,
                expressao,
                linha: contarLinha(conteudo, match.index),
            });
        }
        match = regex.exec(conteudo);
    }

    return declaracoes;
}

function encontrarPropriedadesAcao(conteudo: string): OcorrenciaAcao[] {
    const propriedades: OcorrenciaAcao[] = [];
    const regex = /(?<identificador>pode[A-Z]\w*|habilitar[A-Z]\w*|mostrar[A-Z]\w*|exibir[A-Z]\w*|ocultar[A-Z]\w*|desabilitar[A-Z]\w*|permitir[A-Z]\w*)\s*:\s*(?<expressao>[^,\n}]+)/gu;
    let match = regex.exec(conteudo);

    while (match) {
        const identificador = match.groups?.identificador;
        const expressao = match.groups?.expressao;
        if (identificador && expressao) {
            propriedades.push({
                identificador,
                expressao,
                linha: contarLinha(conteudo, match.index),
            });
        }
        match = regex.exec(conteudo);
    }

    return propriedades;
}

function auditarConteudo({conteudo, arquivoRelativo}: {conteudo: string; arquivoRelativo: string}): ViolacaoAcao[] {
    const violacoes: ViolacaoAcao[] = [];
    const contexto = {arquivoRelativo};

    encontrarDeclaracoes(conteudo).forEach((ocorrencia) => registrarViolacao(violacoes, contexto, ocorrencia));
    encontrarPropriedadesAcao(conteudo).forEach((ocorrencia) => registrarViolacao(violacoes, contexto, ocorrencia));

    return violacoes;
}

async function auditarAcoesBackendFrontend(opcoes: OpcoesAuditoriaAcoes = {}): Promise<ResultadoAuditoriaAcoes> {
    const diretorioBase = path.resolve(opcoes.base ?? DIRETORIO_RAIZ);
    const diretorioFrontend = resolverCaminhoConfigurado("frontend", diretorioBase);
    const caminhoExcecoes = opcoes.excecoes ?? path.join(
        diretorioBase,
        "toolkit",
        "qualidade",
        "frontend-arquitetura",
        "acoes-backend-excecoes.json",
    );
    const excecoes = lerExcecoes(caminhoExcecoes);
    const arquivos = await listarArquivosFrontend(diretorioFrontend);
    const violacoes: ViolacaoAcao[] = [];

    for (const caminhoArquivo of arquivos) {
        const conteudoOriginal = await readFile(caminhoArquivo, "utf-8");
        const conteudo = extrairConteudoAnalise(caminhoArquivo, conteudoOriginal);
        const arquivoRelativo = normalizarCaminho(path.relative(diretorioBase, caminhoArquivo));
        violacoes.push(...auditarConteudo({conteudo, arquivoRelativo}));
    }

    const enriquecidas: ViolacaoEnriquecida[] = violacoes.map((violacao) => ({
        ...violacao,
        dispensada: excecoes.has(chaveViolacao(violacao)),
    }));

    return {
        regra: "frontend-sem-regra-local-acoes",
        caminhoExcecoes: normalizarCaminho(path.relative(diretorioBase, caminhoExcecoes)),
        total: enriquecidas.length,
        dispensadas: enriquecidas.filter((violacao) => violacao.dispensada).length,
        violacoes: enriquecidas.filter((violacao) => !violacao.dispensada),
        todasViolacoes: enriquecidas,
    };
}

export {
    auditarAcoesBackendFrontend,
};
