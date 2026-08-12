import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";

const URL_OPENAPI_PADRAO = "http://127.0.0.1:10000/api-docs";

function resolverCaminhosOpenapi(base = DIRETORIO_RAIZ) {
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

export {
    URL_OPENAPI_PADRAO,
    resolverCaminhosOpenapi,
};
