import path from "node:path";
import {fileURLToPath} from "node:url";

const ARQUIVO_ATUAL = fileURLToPath(import.meta.url);
const DIRETORIO_LIB = path.dirname(ARQUIVO_ATUAL);
const DIRETORIO_TOOLKIT = path.resolve(DIRETORIO_LIB, "..");
const DIRETORIO_RAIZ = path.resolve(DIRETORIO_TOOLKIT, "..");

function resolverNaRaiz(...segmentos) {
    if (segmentos.length > 0 && path.isAbsolute(segmentos[0])) {
        return path.join(...segmentos);
    }
    return path.join(DIRETORIO_RAIZ, ...segmentos);
}

export {
    DIRETORIO_RAIZ,
    DIRETORIO_TOOLKIT,
    resolverNaRaiz
};
