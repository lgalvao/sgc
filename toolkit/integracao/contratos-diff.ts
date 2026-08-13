#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {resolverCaminhosOpenapi} from "./contratos-openapi-caminhos.js";

interface OpcoesDiffContratos {
    base?: string;
    anterior?: string;
    atual?: string;
}

type ModoDiffContratos = "identico" | "diff_textual";

interface ResultadoDiffContratos {
    base: string;
    anterior: string;
    atual: string;
    codigoSaida: 0;
    houveMudancas: boolean;
    modo: ModoDiffContratos;
    stdout: string;
    stderr: string;
}

async function executarDiffContratos({base = DIRETORIO_RAIZ, anterior, atual}: OpcoesDiffContratos = {}): Promise<ResultadoDiffContratos> {
    const caminhos = resolverCaminhosOpenapi(base);
    const anteriorResolvido = anterior ?? caminhos.caminhoReferencia;
    const atualResolvido = atual ?? caminhos.caminhoAtual;
    const [conteudoAnterior, conteudoAtual] = await Promise.all([
        fs.readFile(anteriorResolvido, "utf-8"),
        fs.readFile(atualResolvido, "utf-8")
    ]);

    if (conteudoAnterior === conteudoAtual) {
        return {
            base: caminhos.base,
            anterior: anteriorResolvido,
            atual: atualResolvido,
            codigoSaida: 0,
            houveMudancas: false,
            modo: "identico",
            stdout: "Nenhuma diferença detectada entre a referência e a fotografia atual.",
            stderr: ""
        };
    }

    const resultado = await execa("git", ["diff", "--no-index", "--minimal", "--unified=3", anteriorResolvido, atualResolvido], {
        reject: false,
        cwd: caminhos.base
    });

    const codigoSaida = resultado.exitCode ?? 0;
    if (codigoSaida > 1) {
        throw new Error(resultado.stderr || `git diff terminou com codigo ${codigoSaida}.`);
    }

    return {
        base: caminhos.base,
        anterior: anteriorResolvido,
        atual: atualResolvido,
        codigoSaida: 0,
        houveMudancas: true,
        modo: "diff_textual",
        stdout: resultado.stdout ?? "",
        stderr: resultado.stderr ?? ""
    };
}

function criarResumoMarkdown(resultado: ResultadoDiffContratos): string {
    return [
        "# Diff de contrato OpenAPI",
        "",
        `Anterior: \`${resultado.anterior}\``,
        `Atual: \`${resultado.atual}\``,
        `Modo: ${resultado.modo}`,
        `Houve mudanças: ${resultado.houveMudancas ? "sim" : "não"}`,
        "",
        "## Saída",
        "",
        "```text",
        resultado.stdout || resultado.stderr || "Sem saída.",
        "```",
        ""
    ].join("\n");
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos diff",
            scriptDireto: "integracao/contratos-diff.ts",
            descricao: "Compara duas versões do OpenAPI e produz um resumo útil para revisão de mudanças de contrato.",
            opcoes: [
                "--base <diretorio>   Base do projeto que contém os artefatos padrão.",
                "--anterior <arquivo> Arquivo OpenAPI usado como baseline.",
                "--atual <arquivo>    Arquivo OpenAPI atual.",
                "--json               Emite o resultado em JSON.",
                "--gravar             Grava o resumo Markdown."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts integracao contratos diff",
                "npx tsx toolkit/sgc.ts integracao contratos diff --anterior /tmp/old.json --atual /tmp/new.json",
                "npx tsx toolkit/sgc.ts integracao contratos diff --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const gravar = argumentos.includes("--gravar");
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const caminhos = resolverCaminhosOpenapi(base);
    const anterior = lerOpcao(argumentos, "--anterior", caminhos.caminhoReferencia) ?? caminhos.caminhoReferencia;
    const atual = lerOpcao(argumentos, "--atual", caminhos.caminhoAtual) ?? caminhos.caminhoAtual;

    if (!emitirJson) {
        imprimirCabecalho("DIFF DE CONTRATO OPENAPI");
        escreverLinha(`Anterior: ${pc.dim(anterior)}`);
        escreverLinha(`Atual: ${pc.dim(atual)}`);
    }

    const resultado = await executarDiffContratos({base, anterior, atual});

    if (gravar) {
        await fs.mkdir(path.dirname(caminhos.caminhoRelatorio), {recursive: true});
        await fs.writeFile(caminhos.caminhoRelatorio, criarResumoMarkdown(resultado), "utf-8");
        if (!emitirJson) {
            escreverLinha(`Resumo Markdown: ${pc.dim(caminhos.caminhoRelatorio)}`);
        }
    }

    if (emitirJson) {
        imprimirJson(resultado);
    } else if (resultado.stdout) {
        escreverLinha(resultado.stdout);
    } else if (resultado.stderr) {
        escreverLinha(resultado.stderr);
    } else {
        escreverLinha("Nenhuma diferença textual reportada pelo git diff.");
    }

}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        process.stderr.write(`Erro ao comparar contratos OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    executarDiffContratos,
    principal
};
