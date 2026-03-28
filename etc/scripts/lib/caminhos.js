import path from "node:path";
import {fileURLToPath} from "node:url";

const ARQUIVO_ATUAL = fileURLToPath(import.meta.url);
const DIRETORIO_LIB = path.dirname(ARQUIVO_ATUAL);
const DIRETORIO_SCRIPTS = path.resolve(DIRETORIO_LIB, "..");
const DIRETORIO_RAIZ = path.resolve(DIRETORIO_SCRIPTS, "..", "..");

function resolverNaRaiz(...segmentos) {
    return path.join(DIRETORIO_RAIZ, ...segmentos);
}

export {
    DIRETORIO_RAIZ,
    DIRETORIO_SCRIPTS,
    resolverNaRaiz
};
