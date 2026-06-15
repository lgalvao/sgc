#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_BASELINE, CAMINHO_OPENAPI_LATEST} from "./contratos-openapi-caminhos.js";

const CAMINHO_OPENAPI_DIFF = resolverNaRaiz("toolkit/node_modules/.bin/openapi-diff");
const CAMINHO_RELATORIO_MD = resolverNaRaiz("toolkit/qualidade/openapi/latest/diff-openapi.md");

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
            stdout: "Nenhuma diferença detectada entre baseline e snapshot atual.",
            stderr: ""
        };
    }

    const openapiAnterior = JSON.parse(conteudoAnterior);
    const openapiAtual = JSON.parse(conteudoAtual);
    const versaoAnterior = String(openapiAnterior.openapi ?? "");
    const versaoAtual = String(openapiAtual.openapi ?? "");

    if (versaoAnterior.startsWith("3.1") || versaoAtual.startsWith("3.1")) {
        const diffTexto = await execa("git", ["diff", "--no-index", "--minimal", "--unified=3", anterior, atual], {
            reject: false,
            cwd: resolverNaRaiz(".")
        });

        return {
            anterior,
            atual,
            codigoSaida: 0,
            houveMudancas: true,
            modo: "fallback_textual_openapi_3_1",
            stdout: [
                "Comparação semântica indisponível: o pacote `openapi-diff` atual não suporta OpenAPI 3.1.",
                "Aplicando fallback textual com `git diff --no-index`.",
                "",
                diffTexto.stdout || diffTexto.stderr || "Diferenças detectadas, mas sem saída textual adicional."
            ].join("\n"),
            stderr: ""
        };
    }

    const resultado = await execa(CAMINHO_OPENAPI_DIFF, [anterior, atual], {
        reject: false,
        cwd: resolverNaRaiz(".")
    });

    return {
        anterior,
        atual,
        codigoSaida: resultado.exitCode ?? 0,
        houveMudancas: Boolean((resultado.stdout ?? "").trim()),
        modo: "openapi_diff",
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

    imprimirCabecalho("DIFF DE CONTRATO OPENAPI");
    escreverLinha(`Anterior: ${pc.dim(anterior)}`);
    escreverLinha(`Atual: ${pc.dim(atual)}`);

    const resultado = await executarDiffContratos({anterior, atual});

    if (!semGravar) {
        await fs.mkdir(path.dirname(CAMINHO_RELATORIO_MD), {recursive: true});
        await fs.writeFile(CAMINHO_RELATORIO_MD, criarResumoMarkdown(resultado), "utf-8");
        escreverLinha(`Resumo Markdown: ${pc.dim(CAMINHO_RELATORIO_MD)}`);
    }

    if (resultado.stdout) {
        escreverLinha(resultado.stdout);
    } else if (resultado.stderr) {
        escreverLinha(resultado.stderr);
    } else {
        escreverLinha("Nenhuma diferença textual reportada pelo openapi-diff.");
    }

    if (emitirJson) {
        imprimirJson(resultado);
    }

}

main().catch((erro) => {
    escreverLinha(pc.red(`Erro ao comparar contratos OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}`));
    process.exit(1);
});
