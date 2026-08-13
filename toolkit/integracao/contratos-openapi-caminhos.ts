import path from "node:path";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {resolverCaminhoConfigurado} from "../biblioteca/configuracao.js";

interface CaminhosOpenapi {
    base: string;
    diretorioOpenapi: string;
    caminhoAtual: string;
    caminhoReferencia: string;
    caminhoRelatorio: string;
}

function resolverCaminhosOpenapi(base: string = DIRETORIO_RAIZ): CaminhosOpenapi {
    const baseResolvida = path.resolve(base ?? DIRETORIO_RAIZ);
    const diretorioOpenapi = resolverCaminhoConfigurado("contratosOpenapi", baseResolvida);

    return {
        base: baseResolvida,
        diretorioOpenapi,
        caminhoAtual: path.join(diretorioOpenapi, "mais-recente", "openapi.json"),
        caminhoReferencia: path.join(diretorioOpenapi, "referencia", "openapi.json"),
        caminhoRelatorio: path.join(diretorioOpenapi, "mais-recente", "diferencas.md"),
    };
}

function resolverCaminhoArquivoOpenapi(base: string, caminho: string): string {
    return path.isAbsolute(caminho) ? caminho : path.resolve(base, caminho);
}

export {
    resolverCaminhoArquivoOpenapi,
    resolverCaminhosOpenapi,
};
