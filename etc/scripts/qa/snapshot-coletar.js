#!/usr/bin/env node
import {executarNode} from "../lib/execucao.js";
import {pathToFileURL} from "node:url";
import logger from "../lib/logger.js";

const PERFIS_VALIDOS = new Set(["rapido", "completo", "backend", "frontend"]);

function normalizarArgumentosSnapshot(argumentos = []) {
    const resultado = [];

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const atual = argumentos[indice];

        if (atual === "--help" || atual === "-h") {
            resultado.push("--ajuda");
            continue;
        }

        if (atual === "--perfil") {
            const perfil = argumentos[indice + 1];
            if (!perfil) {
                throw new Error("Informe um valor para --perfil (rapido, completo, backend ou frontend).");
            }

            if (!PERFIS_VALIDOS.has(perfil)) {
                throw new Error(`Perfil invalido: ${perfil}. Use rapido, completo, backend ou frontend.`);
            }

            resultado.push("--perfil", perfil);
            indice += 1;
            continue;
        }

        if (atual.startsWith("--perfil=")) {
            const perfil = atual.split("=", 2)[1];
            if (!PERFIS_VALIDOS.has(perfil)) {
                throw new Error(`Perfil invalido: ${perfil}. Use rapido, completo, backend ou frontend.`);
            }
        }

        resultado.push(atual);
    }

    return resultado;
}

async function executarSnapshotQa(argumentos = []) {
    const argumentosNormalizados = normalizarArgumentosSnapshot(argumentos);
    await executarNode("etc/scripts/qa/snapshot-coletar-execucao.mjs", argumentosNormalizados);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    executarSnapshotQa(process.argv.slice(2)).catch((error) => {
        logger.error(`Erro ao coletar snapshot de QA: ${error.message}`);
        process.exit(1);
    });
}

export {
    executarSnapshotQa
};
