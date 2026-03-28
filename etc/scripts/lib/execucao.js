import {existsSync} from "node:fs";
import {execaNode} from "execa";
import pc from "picocolors";
import {DIRETORIO_RAIZ, resolverNaRaiz} from "./caminhos.js";

function garantirArquivo(relativo) {
    const absoluto = resolverNaRaiz(relativo);
    if (!existsSync(absoluto)) {
        throw new Error(`Script nao encontrado: ${relativo}`);
    }
    return absoluto;
}

async function executarNode(relativo, args = []) {
    const script = garantirArquivo(relativo);
    return execaNode(script, args, {
        cwd: DIRETORIO_RAIZ,
        stdio: "inherit"
    });
}

function imprimirSecao(titulo) {
    console.log(pc.bold(pc.cyan(`\n${titulo}`)));
}

export {
    executarNode,
    garantirArquivo,
    imprimirSecao
};
