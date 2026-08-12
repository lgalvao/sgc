#!/usr/bin/env node
import {execa} from "execa";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";

const CAMINHO_ESPECIFICACAO = "e2e/a11y/crawler.spec.ts";

function normalizarArgumentosPlaywright(argumentos) {
    return argumentos.map((argumento) => {
        if (argumento === "--projeto") {
            return "--project";
        }
        if (argumento.startsWith("--projeto=")) {
            return argumento.replace("--projeto=", "--project=");
        }
        if (argumento === "--lista") {
            return "--list";
        }
        return argumento;
    });
}

async function executarCrawler(argumentos = []) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "frontend acessibilidade crawler",
            scriptDireto: "frontend/acessibilidade-crawler.js",
            descricao: "Executa o crawler de acessibilidade usando o executor do Playwright.",
            opcoes: [
                "--projeto <nome>   Projeto Playwright a ser executado.",
                "--lista             Lista os testes sem executa-los."
            ],
            exemplos: [
                "node toolkit/sgc.js frontend acessibilidade crawler",
                "node toolkit/sgc.js frontend acessibilidade crawler --projeto chromium"
            ]
        });
        return;
    }

    const argumentosPlaywright = [
        "playwright",
        "test",
        CAMINHO_ESPECIFICACAO,
        "--config=e2e/playwright.config.ts",
        ...normalizarArgumentosPlaywright(argumentos)
    ];

    await execa("npx", argumentosPlaywright, {
        cwd: resolverNaRaiz(),
        stdio: "inherit",
        shell: process.platform === "win32"
    });
}

async function principal(argumentos = process.argv.slice(2)) {
    return executarCrawler(argumentos);
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        process.stderr.write(`Erro ao executar crawler de acessibilidade: ${mensagem}\n`);
        process.exitCode = 1;
    });
}

export {
    executarCrawler,
    principal,
};
