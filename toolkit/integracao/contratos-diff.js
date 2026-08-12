#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {pathToFileURL} from "node:url";
import {execa} from "execa";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_BASELINE, CAMINHO_OPENAPI_LATEST, CAMINHO_RELATORIO_OPENAPI} from "./contratos-openapi-caminhos.js";

const CAMINHO_RELATORIO_MD = CAMINHO_RELATORIO_OPENAPI;

function lerOpcao(args, nome, padrao) {
    const indice = args.indexOf(nome);
    if (indice === -1) {
        return padrao;
    }
    const valor = args[indice + 1];
    if (!valor || valor.startsWith("--")) {
        throw new Error(`Informe um valor para ${nome}.`);
    }
    return valor;
}

async function executarDiffContratos({anterior = CAMINHO_OPENAPI_BASELINE, atual = CAMINHO_OPENAPI_LATEST}) {
    const [conteudoAnterior, conteudoAtual] = await Promise.all([
        fs.readFile(anterior, "utf-8"),
        fs.readFile(atual, "utf-8")
    ]);

    if (conteudoAnterior === conteudoAtual) {
        return {
            anterior,
            atual,
            codigoSaida: 0,
            houveMudancas: false,
            modo: "identico",
            stdout: "Nenhuma diferença detectada entre a referência e a fotografia atual.",
            stderr: ""
        };
    }

    const resultado = await execa("git", ["diff", "--no-index", "--minimal", "--unified=3", anterior, atual], {
        reject: false,
        cwd: resolverNaRaiz(".")
    });

    if (resultado.exitCode > 1) {
        throw new Error(resultado.stderr || `git diff terminou com codigo ${resultado.exitCode}.`);
    }

    return {
        anterior,
        atual,
        codigoSaida: 0,
        houveMudancas: true,
        modo: "diff_textual",
        stdout: resultado.stdout ?? "",
        stderr: resultado.stderr ?? ""
    };
}

function criarResumoMarkdown(resultado) {
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

async function main() {
    const args = process.argv.slice(2);
    if (args.includes("--help") || args.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos diff",
            scriptDireto: "integracao/contratos-diff.js",
            descricao: "Compara duas versões do OpenAPI e produz um resumo útil para revisão de mudanças de contrato.",
            opcoes: [
                "--anterior <arquivo> Arquivo OpenAPI usado como baseline.",
                "--atual <arquivo>    Arquivo OpenAPI atual.",
                "--json               Emite o resultado em JSON.",
                "--sem-gravar         Não grava o resumo Markdown."
            ],
            exemplos: [
                "node toolkit/sgc.js integracao contratos diff",
                "node toolkit/sgc.js integracao contratos diff --anterior /tmp/old.json --atual /tmp/new.json",
                "node toolkit/sgc.js integracao contratos diff --json"
            ]
        });
        return;
    }

    const emitirJson = args.includes("--json");
    const semGravar = args.includes("--sem-gravar");
    const anterior = lerOpcao(args, "--anterior", CAMINHO_OPENAPI_BASELINE);
    const atual = lerOpcao(args, "--atual", CAMINHO_OPENAPI_LATEST);

    if (!emitirJson) {
        imprimirCabecalho("DIFF DE CONTRATO OPENAPI");
        escreverLinha(`Anterior: ${pc.dim(anterior)}`);
        escreverLinha(`Atual: ${pc.dim(atual)}`);
    }

    const resultado = await executarDiffContratos({anterior, atual});

    if (!semGravar) {
        await fs.mkdir(path.dirname(CAMINHO_RELATORIO_MD), {recursive: true});
        await fs.writeFile(CAMINHO_RELATORIO_MD, criarResumoMarkdown(resultado), "utf-8");
        if (!emitirJson) {
            escreverLinha(`Resumo Markdown: ${pc.dim(CAMINHO_RELATORIO_MD)}`);
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

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        process.stderr.write(`Erro ao comparar contratos OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exit(1);
    });
}
