#!/usr/bin/env node
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {executarNode} from "../../lib/execucao.js";

async function executarAliasLegado(argumentos = []) {
    process.stderr.write(
        `${pc.yellow("Aviso:")} script legado 'projeto/sincronizar-versao.js'. Use ` +
        "'projeto/versao-sincronizar.js'.\n"
    );
    await executarNode("toolkit/projeto/versao-sincronizar.js", argumentos);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    executarAliasLegado(process.argv.slice(2)).catch((erro) => {
        process.stderr.write(`${pc.red(`Erro ao executar alias legado: ${erro.message}`)}\n`);
        process.exit(1);
    });
}

export {
    executarAliasLegado
};
