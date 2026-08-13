import {existsSync, readFileSync} from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ, DIRETORIO_TOOLKIT} from "./caminhos.js";

type DiretoriosConfigurados = Record<string, string>;

interface ConfiguracaoToolkit {
    versao: 1;
    diretorios: DiretoriosConfigurados;
}

interface ConfiguracaoSobreposta {
    versao: 1;
    diretorios?: DiretoriosConfigurados;
}

const NOME_ARQUIVO_CONFIGURACAO = "configuracao-toolkit.json";
const VERSAO_CONFIGURACAO = 1 as const;
const DIRETORIOS_FORNECIDOS_PELO_TOOLKIT = new Set(["regrasSemgrep"]);

const CONFIGURACAO_PADRAO: ConfiguracaoToolkit = {
    versao: VERSAO_CONFIGURACAO,
    diretorios: {
        backend: "backend",
        frontend: "frontend",
        backendCodigo: "backend/src/main/java/sgc",
        backendTestes: "backend/src/test/java",
        frontendCodigo: "frontend/src",
        testesIntegracao: "e2e",
        artefatosQualidade: "toolkit/qualidade/artefatos",
        coberturaBackend: "backend/build/reports/jacoco/test/jacocoTestReport.xml",
        coberturaFrontend: "frontend/coverage/coverage-final.json",
        orcamentoResiduosFrontend: "toolkit/qualidade/politicas/frontend-residuos/orcamento.json",
        excecoesResiduosFrontend: "toolkit/qualidade/politicas/frontend-residuos/excecoes.json",
        regrasSemgrep: "toolkit/qualidade/politicas/semgrep/sgc-qualidade.yml",
        contratosOpenapi: "toolkit/qualidade/artefatos/openapi"
    }
};

function ehObjeto(valor: unknown): valor is Record<string, unknown> {
    return typeof valor === "object" && valor !== null && !Array.isArray(valor);
}

function validarConfiguracao(valor: unknown): ConfiguracaoSobreposta {
    if (!ehObjeto(valor)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} deve conter um objeto JSON.`);
    }

    const chavesPermitidas = new Set(["versao", "diretorios"]);
    const chavesDesconhecidas = Object.keys(valor).filter(chave => !chavesPermitidas.has(chave));
    if (chavesDesconhecidas.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} possui chave(s) desconhecida(s): ${chavesDesconhecidas.join(", ")}.`);
    }

    const versao = valor.versao;
    if (versao !== VERSAO_CONFIGURACAO) {
        if (versao === undefined) {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} deve informar a versão ${VERSAO_CONFIGURACAO}.`);
        }
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO} possui versão ${String(versao)}; a versão suportada é ${VERSAO_CONFIGURACAO}.`);
    }

    const diretorios = valor.diretorios;
    if (diretorios === undefined) {
        return {versao};
    }
    if (!ehObjeto(diretorios)) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios deve ser um objeto JSON.`);
    }

    const nomesPermitidos = new Set(Object.keys(CONFIGURACAO_PADRAO.diretorios));
    const nomesDesconhecidos = Object.keys(diretorios).filter(nome => !nomesPermitidos.has(nome));
    if (nomesDesconhecidos.length > 0) {
        throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios possui nome(s) desconhecido(s): ${nomesDesconhecidos.join(", ")}.`);
    }

    const diretoriosValidados: DiretoriosConfigurados = {};
    for (const [nome, caminho] of Object.entries(diretorios)) {
        if (typeof caminho !== "string" || caminho.trim() === "") {
            throw new Error(`${NOME_ARQUIVO_CONFIGURACAO}.diretorios.${nome} deve ser um caminho textual não vazio.`);
        }
        diretoriosValidados[nome] = caminho;
    }

    return {
        versao: VERSAO_CONFIGURACAO,
        diretorios: diretoriosValidados
    };
}

function combinarConfiguracoes(
    configuracaoBase: ConfiguracaoToolkit,
    configuracaoSobreposta: ConfiguracaoSobreposta
): ConfiguracaoToolkit {
    return {
        ...configuracaoBase,
        ...configuracaoSobreposta,
        diretorios: {
            ...configuracaoBase.diretorios,
            ...configuracaoSobreposta.diretorios ?? {}
        }
    };
}

function carregarConfiguracao(diretorioBase = DIRETORIO_RAIZ): ConfiguracaoToolkit {
    const caminho = path.join(diretorioBase, NOME_ARQUIVO_CONFIGURACAO);
    if (!existsSync(caminho)) {
        return CONFIGURACAO_PADRAO;
    }

    let configuracaoSobreposta: ConfiguracaoSobreposta;
    try {
        configuracaoSobreposta = validarConfiguracao(JSON.parse(readFileSync(caminho, "utf8")));
    } catch (erro: unknown) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        throw new Error(`Nao foi possivel validar ${NOME_ARQUIVO_CONFIGURACAO}: ${mensagem}`, {cause: erro});
    }

    return combinarConfiguracoes(CONFIGURACAO_PADRAO, configuracaoSobreposta);
}

function resolverCaminhoConfigurado(nomeDiretorio: string, diretorioBase = DIRETORIO_RAIZ): string {
    const configuracao = carregarConfiguracao(diretorioBase);
    const caminhoRelativo = configuracao.diretorios[nomeDiretorio];
    if (!caminhoRelativo) {
        throw new Error(`Diretorio configurado desconhecido: ${nomeDiretorio}`);
    }

    const caminhoPadrao = CONFIGURACAO_PADRAO.diretorios[nomeDiretorio];
    if (DIRETORIOS_FORNECIDOS_PELO_TOOLKIT.has(nomeDiretorio) && caminhoRelativo === caminhoPadrao) {
        const caminhoRelativoNoToolkit = caminhoRelativo.startsWith("toolkit/")
            ? caminhoRelativo.slice("toolkit/".length)
            : caminhoRelativo;
        const caminhoDoToolkit = path.resolve(DIRETORIO_TOOLKIT, caminhoRelativoNoToolkit);
        if (existsSync(caminhoDoToolkit)) {
            return caminhoDoToolkit;
        }
    }

    return path.resolve(diretorioBase, caminhoRelativo);
}

export {
    CONFIGURACAO_PADRAO,
    NOME_ARQUIVO_CONFIGURACAO,
    VERSAO_CONFIGURACAO,
    carregarConfiguracao,
    resolverCaminhoConfigurado,
    validarConfiguracao
};
