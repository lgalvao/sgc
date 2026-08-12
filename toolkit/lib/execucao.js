import {existsSync} from "node:fs";
import {execaNode} from "execa";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "./caminhos.js";

/**
 * @param {string} relativo
 * @returns {string}
 */
function garantirArquivo(relativo) {
    const absoluto = resolverNaRaiz(relativo);
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
    executarNode,
    garantirArquivo
};
