import {existsSync} from "node:fs";
import path from "node:path";
import {fileURLToPath, pathToFileURL} from "node:url";
import {execaNode} from "execa";
import {DIRETORIO_RAIZ} from "./caminhos.js";

const DIRETORIO_EXECUCAO_TOOLKIT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "..");

/**
 * @param {string} urlModulo
 * @returns {boolean}
 */
function ehEntradaPrincipal(urlModulo) {
    return Boolean(process.argv[1] && urlModulo === pathToFileURL(process.argv[1]).href);
}

/**
 * @param {string} relativo
 * @returns {string}
 */
function garantirArquivo(relativo) {
    const caminhoRelativo = relativo.startsWith("toolkit/")
        ? relativo.slice("toolkit/".length)
        : relativo;
    const absoluto = path.resolve(DIRETORIO_EXECUCAO_TOOLKIT, caminhoRelativo);
    if (!existsSync(absoluto)) {
        throw new Error(`Script nao encontrado: ${relativo}`);
    }
    return absoluto;
}

/**
 * @param {string} relativo
 * @param {string[]} [argumentos]
 */
async function executarNode(relativo, argumentos = []) {
    const script = garantirArquivo(relativo);
    return execaNode(script, argumentos, {
        cwd: DIRETORIO_RAIZ,
        stdio: "inherit"
    });
}

export {
    ehEntradaPrincipal,
    executarNode,
    garantirArquivo
};
