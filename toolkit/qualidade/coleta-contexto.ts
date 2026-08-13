import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";
import {obterDiretorioArtefatos} from "../biblioteca/qualidade.js";

interface ContextoColeta {
    base: string;
    diretorioArtefatos: string;
    diretorioExecucoes: string;
    diretorioMaisRecente: string;
    diretorioBackend: string;
    diretorioFrontend: string;
    diretorioFrontendCodigo: string;
}

function criarContextoColeta(base: string = DIRETORIO_RAIZ): ContextoColeta {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const diretorioArtefatos = obterDiretorioArtefatos(baseResolvida);

    return {
        base: baseResolvida,
        diretorioArtefatos,
        diretorioExecucoes: path.join(diretorioArtefatos, "execucoes"),
        diretorioMaisRecente: path.join(diretorioArtefatos, "mais-recente"),
        diretorioBackend: resolverCaminhoConfigurado("backend", baseResolvida),
        diretorioFrontend: resolverCaminhoConfigurado("frontend", baseResolvida),
        diretorioFrontendCodigo: resolverCaminhoConfigurado("frontendCodigo", baseResolvida),
    };
}

export {criarContextoColeta, type ContextoColeta};
