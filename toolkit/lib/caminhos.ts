import path from "node:path";
import {fileURLToPath} from "node:url";

const ARQUIVO_ATUAL = fileURLToPath(import.meta.url);
const DIRETORIO_LIB = path.dirname(ARQUIVO_ATUAL);
const DIRETORIO_TOOLKIT_FISICO = path.resolve(DIRETORIO_LIB, "..");
const DIRETORIO_TOOLKIT = path.basename(DIRETORIO_TOOLKIT_FISICO) === "dist"
    ? path.resolve(DIRETORIO_TOOLKIT_FISICO, "..")
    : DIRETORIO_TOOLKIT_FISICO;
const DIRETORIO_RAIZ = process.cwd();

/**
 * Resolve segmentos relativos ao diretório de trabalho do projeto auditado.
 */
function resolverNaRaiz(...segmentos: string[]): string {
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
