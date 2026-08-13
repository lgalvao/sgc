#!/usr/bin/env node
import path from "node:path";
import {execa} from "execa";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";

const CAMINHO_ESPECIFICACAO = "e2e/a11y/crawler.spec.ts";
const CAMINHO_CONFIGURACAO = "e2e/playwright.config.ts";

interface OpcoesCrawler {
    base?: string;
    especificacao?: string;
    configuracao?: string;
    executarComando?: ExecutarComando;
}

interface ResultadoCrawler {
    diretorioBase: string;
    especificacao: string;
    configuracao: string;
    argumentos: string[];
}

type ExecutarComando = (comando: string, argumentos: readonly string[], diretorioBase: string) => Promise<void>;

function normalizarArgumentosPlaywright(argumentos: string[]): string[] {
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

function removerOpcoesLocais(argumentos: string[]): string[] {
    const nomes = new Set(["--base", "--especificacao", "--configuracao"]);
    const resultado: string[] = [];
    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const argumento = argumentos[indice];
        const nome = argumento.split("=", 1)[0];
        if (!nomes.has(nome)) {
            resultado.push(argumento);
            continue;
        }
        if (!argumento.includes("=")) {
            indice += 1;
        }
    }
    return resultado;
}

function caminhoRelativo(diretorioBase: string, caminho: string): string {
    return path.relative(diretorioBase, path.resolve(diretorioBase, caminho)).replaceAll("\\", "/");
}

const executarComandoPadrao: ExecutarComando = async (comando, argumentos, diretorioBase) => {
    await execa(comando, argumentos, {
        cwd: diretorioBase,
        stdio: "inherit",
        shell: process.platform === "win32"
    });
};

async function executarCrawler(
    argumentos: string[] = [],
    opcoes: OpcoesCrawler = {}
): Promise<ResultadoCrawler | undefined> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "frontend acessibilidade crawler",
            scriptDireto: "frontend/acessibilidade-crawler.ts",
            descricao: "Executa o crawler de acessibilidade usando o executor do Playwright.",
            opcoes: [
                "--projeto <nome>   Projeto Playwright a ser executado.",
                "--lista             Lista os testes sem executa-los.",
                "--base <diretorio>  Base do projeto auditado.",
                "--especificacao <arquivo> Especificacao Playwright alternativa.",
                "--configuracao <arquivo> Configuracao Playwright alternativa."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend acessibilidade crawler",
                "npx tsx toolkit/sgc.ts frontend acessibilidade crawler --projeto chromium"
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(opcoes.base ?? resolverNaRaiz());
    const especificacao = caminhoRelativo(diretorioBase, opcoes.especificacao ?? CAMINHO_ESPECIFICACAO);
    const configuracao = caminhoRelativo(diretorioBase, opcoes.configuracao ?? CAMINHO_CONFIGURACAO);
    const argumentosPlaywright = [
        "playwright",
        "test",
        especificacao,
        `--config=${configuracao}`,
        ...normalizarArgumentosPlaywright(removerOpcoesLocais(argumentos))
    ];

    await (opcoes.executarComando ?? executarComandoPadrao)("npx", argumentosPlaywright, diretorioBase);
    return {diretorioBase, especificacao, configuracao, argumentos: argumentosPlaywright};
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<ResultadoCrawler | undefined> {
    return executarCrawler(argumentos, {
        base: lerOpcao(argumentos, "--base", undefined),
        especificacao: lerOpcao(argumentos, "--especificacao", undefined),
        configuracao: lerOpcao(argumentos, "--configuracao", undefined)
    });
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
    normalizarArgumentosPlaywright,
    principal,
};
