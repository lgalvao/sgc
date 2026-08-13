import path from "node:path";
import {caminhoRaizProjeto, ehEntradaPrincipal, executarComandoPadrao, exibirAjuda, lerOpcao, removerOpcoesLocais} from "./lib/cli.js";

const CAMINHO_RELATIVO_ESPECIFICACAO = path.join("e2e", "a11y", "crawler.spec.ts");
const CAMINHO_RELATIVO_CONFIGURACAO = path.join("e2e", "playwright.config.ts");

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

type ExecutarComando = (comando: string, argumentos: readonly string[], diretorioBase: string) => Promise<{codigoSaida: number}>;

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

function caminhoRelativo(diretorioBase: string, caminho: string): string {
    return path.relative(diretorioBase, path.resolve(diretorioBase, caminho)).replaceAll("\\", "/");
}

function obterCaminhosPadrao(diretorioBase: string): {especificacao: string; configuracao: string} {
    return {
        especificacao: caminhoRelativo(diretorioBase, CAMINHO_RELATIVO_ESPECIFICACAO),
        configuracao: caminhoRelativo(diretorioBase, CAMINHO_RELATIVO_CONFIGURACAO),
    };
}

async function executarCrawler(
    argumentos: string[] = [],
    opcoes: OpcoesCrawler = {}
): Promise<ResultadoCrawler | undefined> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjuda({
            uso: "npx tsx e2e/acessibilidade-crawler.ts",
            descricao: "Executa o crawler de acessibilidade usando o executor do Playwright.",
            opcoes: [
                "--projeto <nome>   Projeto Playwright a ser executado.",
                "--lista             Lista os testes sem executa-los.",
                "--base <diretorio>  Base do projeto auditado.",
                "--especificacao <arquivo> Especificacao Playwright alternativa.",
                "--configuracao <arquivo> Configuracao Playwright alternativa."
            ],
            exemplos: [
                "npx tsx e2e/acessibilidade-crawler.ts",
                "npx tsx e2e/acessibilidade-crawler.ts --projeto chromium"
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(opcoes.base ?? caminhoRaizProjeto());
    const caminhosPadrao = obterCaminhosPadrao(diretorioBase);
    const especificacao = caminhoRelativo(diretorioBase, opcoes.especificacao ?? caminhosPadrao.especificacao);
    const configuracao = caminhoRelativo(diretorioBase, opcoes.configuracao ?? caminhosPadrao.configuracao);
    const argumentosPlaywright = [
        "playwright",
        "test",
        especificacao,
        `--config=${configuracao}`,
        ...normalizarArgumentosPlaywright(removerOpcoesLocais(argumentos, new Set(["--base", "--especificacao", "--configuracao"])))
    ];

    const resultadoExecucao = await (opcoes.executarComando ?? executarComandoPadrao)("npx", argumentosPlaywright, diretorioBase);
    if (resultadoExecucao.codigoSaida !== 0) {
        process.exitCode = resultadoExecucao.codigoSaida;
    }
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
        console.error(`Erro ao executar crawler de acessibilidade: ${mensagem}`);
        process.exitCode = 1;
    });
}

export {
    executarCrawler,
    normalizarArgumentosPlaywright,
    principal,
};
