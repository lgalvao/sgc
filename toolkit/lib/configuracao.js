import fs from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "./caminhos.js";

const NOME_ARQUIVO_CONFIGURACAO = "configuracao-toolkit.json";

/** @typedef {Record<string, string>} DiretoriosConfigurados */
/** @typedef {{diretorios: DiretoriosConfigurados}} ConfiguracaoToolkit */
/** @typedef {{diretorios?: DiretoriosConfigurados}} ConfiguracaoSobreposta */

/** @type {ConfiguracaoToolkit} */
const CONFIGURACAO_PADRAO = {
    diretorios: {
        backend: "backend",
        frontend: "frontend",
        backendCodigo: "backend/src/main/java/sgc",
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

/**
 * @param {ConfiguracaoToolkit} configuracaoBase
 * @param {ConfiguracaoSobreposta} configuracaoSobreposta
 * @returns {ConfiguracaoToolkit}
 */
function combinarConfiguracoes(configuracaoBase, configuracaoSobreposta) {
    return {
        ...configuracaoBase,
        ...configuracaoSobreposta,
        diretorios: {
            ...configuracaoBase.diretorios,
            ...configuracaoSobreposta.diretorios ?? {}
        }
    };
}

/**
 * @param {string} [diretorioBase]
 * @returns {ConfiguracaoToolkit}
 */
function carregarConfiguracao(diretorioBase = DIRETORIO_RAIZ) {
    const caminho = path.join(diretorioBase, NOME_ARQUIVO_CONFIGURACAO);
    if (!fs.existsSync(caminho)) {
        return CONFIGURACAO_PADRAO;
    }

    let configuracaoSobreposta;
    try {
        configuracaoSobreposta = /** @type {ConfiguracaoSobreposta} */ (JSON.parse(fs.readFileSync(caminho, "utf8")));
    } catch (erro) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        throw new Error(`Nao foi possivel ler ${NOME_ARQUIVO_CONFIGURACAO}: ${mensagem}`, {cause: erro});
    }

    return combinarConfiguracoes(CONFIGURACAO_PADRAO, configuracaoSobreposta);
}

/**
 * @param {string} nomeDiretorio
 * @param {string} [diretorioBase]
 * @returns {string}
 */
function resolverCaminhoConfigurado(nomeDiretorio, diretorioBase = DIRETORIO_RAIZ) {
    const configuracao = carregarConfiguracao(diretorioBase);
    const caminhoRelativo = configuracao.diretorios[nomeDiretorio];
    if (!caminhoRelativo) {
        throw new Error(`Diretorio configurado desconhecido: ${nomeDiretorio}`);
    }

    return path.resolve(diretorioBase, caminhoRelativo);
}

export {
    CONFIGURACAO_PADRAO,
    NOME_ARQUIVO_CONFIGURACAO,
    carregarConfiguracao,
    resolverCaminhoConfigurado
};
