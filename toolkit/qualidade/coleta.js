#!/usr/bin/env node
import {executarNode} from "../lib/execucao.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
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

        if (atual === "--base") {
            const base = argumentos[indice + 1];
            if (!base) {
                throw new Error("Informe um valor para --base.");
            }

            resultado.push("--base", base);
            indice += 1;
            continue;
        }

        if (atual.startsWith("--base=") && atual.slice("--base=".length).length === 0) {
            throw new Error("Informe um valor para --base.");
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
                "--perfil <perfil>   Perfil de execucao: rapido, completo, backend ou frontend.",
                "--base <diretorio>  Sobrescreve o diretorio base do projeto auditado."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts qualidade coletar --perfil rapido",
                "npx tsx toolkit/qualidade/coleta.js --perfil frontend"
            ]
        });
        return;
    }

    const argumentosNormalizados = normalizarArgumentosColeta(argumentos);
    await executarNode("toolkit/qualidade/coleta-execucao.js", argumentosNormalizados);
}

if (ehEntradaPrincipal(import.meta.url)) {
    executarColetaQualidade().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        logger.error(`Erro ao coletar fotografia de qualidade: ${mensagem}`);
        process.exitCode = 1;
    });
}

export {
    executarColetaQualidade
};
