import fs from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "./caminhos.js";

const NOME_ARQUIVO_CONFIGURACAO = "configuracao-toolkit.json";

const CONFIGURACAO_PADRAO = {
    diretorios: {
        backend: "backend",
        frontend: "frontend",
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

function combinarConfiguracoes(configuracaoBase, configuracaoSobreposta) {
    return {
        ...configuracaoBase,
        ...configuracaoSobreposta,
        diretorios: {
            ...configuracaoBase.diretorios,
            ...configuracaoSobreposta.diretorios
        }
    };
}

function carregarConfiguracao(diretorioBase = DIRETORIO_RAIZ) {
    const caminho = path.join(diretorioBase, NOME_ARQUIVO_CONFIGURACAO);
    if (!fs.existsSync(caminho)) {
        return CONFIGURACAO_PADRAO;
    }

    let configuracaoSobreposta;
    try {
        configuracaoSobreposta = JSON.parse(fs.readFileSync(caminho, "utf8"));
    } catch (erro) {
        throw new Error(`Nao foi possivel ler ${NOME_ARQUIVO_CONFIGURACAO}: ${erro.message}`, {cause: erro});
    }

    return combinarConfiguracoes(CONFIGURACAO_PADRAO, configuracaoSobreposta);
}

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
