import {existsSync, readFileSync} from "node:fs";
import path from "node:path";
import {DIRETORIO_RAIZ} from "./caminhos.js";

type DiretoriosConfigurados = Record<string, string>;

interface ConfiguracaoToolkit {
    diretorios: DiretoriosConfigurados;
}

interface ConfiguracaoSobreposta {
    diretorios?: DiretoriosConfigurados;
}

const NOME_ARQUIVO_CONFIGURACAO = "configuracao-toolkit.json";

const CONFIGURACAO_PADRAO: ConfiguracaoToolkit = {
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
        configuracaoSobreposta = JSON.parse(readFileSync(caminho, "utf8")) as ConfiguracaoSobreposta;
    } catch (erro: unknown) {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        throw new Error(`Nao foi possivel ler ${NOME_ARQUIVO_CONFIGURACAO}: ${mensagem}`, {cause: erro});
    }

    return combinarConfiguracoes(CONFIGURACAO_PADRAO, configuracaoSobreposta);
}

function resolverCaminhoConfigurado(nomeDiretorio: string, diretorioBase = DIRETORIO_RAIZ): string {
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
