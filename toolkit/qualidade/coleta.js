#!/usr/bin/env node
import {executarNode} from "../lib/execucao.js";
import {pathToFileURL} from "node:url";
import logger from "../lib/logger.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

const PERFIS_VALIDOS = new Set(["rapido", "completo", "backend", "frontend"]);

function normalizarArgumentosColeta(argumentos = []) {
    const resultado = [];

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const atual = argumentos[indice];

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

async function executarColetaQualidade(argumentos = []) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "qualidade coletar",
            scriptDireto: "qualidade/coleta.js",
            descricao: "Coleta uma fotografia consolidada de qualidade do projeto.",
            opcoes: [
                "--perfil <perfil>   Perfil de execucao: rapido, completo, backend ou frontend."
            ],
            exemplos: [
                "node toolkit/sgc.js qualidade coletar --perfil rapido",
                "node toolkit/qualidade/coleta.js --perfil frontend"
            ]
        });
        return;
    }

    const argumentosNormalizados = normalizarArgumentosColeta(argumentos);
    await executarNode("toolkit/qualidade/coleta-execucao.js", argumentosNormalizados);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    executarColetaQualidade(process.argv.slice(2)).catch((error) => {
        logger.error(`Erro ao coletar fotografia de qualidade: ${error.message}`);
        process.exit(1);
    });
}

export {
    executarColetaQualidade
};
