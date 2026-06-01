#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import openapiTS, {astToString, COMMENT_HEADER} from "openapi-typescript";
import pc from "picocolors";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_LATEST, CAMINHO_TIPOS_FRONTEND, URL_OPENAPI_PADRAO} from "./contratos-openapi-caminhos.js";
import {exportarOpenapi} from "./contratos-exportar-openapi.js";

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

async function gerarTiposContrato({
    entrada = CAMINHO_OPENAPI_LATEST,
    saida = CAMINHO_TIPOS_FRONTEND,
    url = URL_OPENAPI_PADRAO
}) {
    try {
        await fs.access(entrada);
    } catch {
        await exportarOpenapi({url, saida: entrada});
    }

    const ast = await openapiTS(new URL(`file://${entrada}`), {
        alphabetize: true,
        defaultNonNullable: true,
        immutable: false
    });

    const cabecalho = [
        "/* eslint-disable */",
        COMMENT_HEADER.trimEnd(),
        ""
    ].join("\n");

    await fs.mkdir(path.dirname(saida), {recursive: true});
    await fs.writeFile(saida, `${cabecalho}${astToString(ast)}`, "utf-8");

    return {
        entrada,
        saida
    };
}

async function main() {
    const args = process.argv.slice(2);
    if (args.includes("--help") || args.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos gerar-tipos",
            scriptDireto: "integracao/contratos-gerar-tipos.js",
            descricao: "Gera tipos TypeScript a partir do OpenAPI do backend para reduzir drift entre contrato HTTP e frontend.",
            opcoes: [
                "--entrada <arquivo>  Arquivo OpenAPI JSON de entrada.",
                "--saida <arquivo>    Arquivo .d.ts de saída no frontend.",
                "--url <url>          URL usada para exportar o OpenAPI se a entrada ainda não existir.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "node etc/scripts/sgc.js integracao contratos gerar-tipos",
                "node etc/scripts/sgc.js integracao contratos gerar-tipos --entrada /tmp/openapi.json",
                "node etc/scripts/sgc.js integracao contratos gerar-tipos --saida frontend/src/generated/sgc-openapi.d.ts --json"
            ]
        });
        return;
    }

    const emitirJson = args.includes("--json");
    const entrada = lerOpcao(args, "--entrada", CAMINHO_OPENAPI_LATEST);
    const saida = lerOpcao(args, "--saida", CAMINHO_TIPOS_FRONTEND);
    const url = lerOpcao(args, "--url", URL_OPENAPI_PADRAO);

    imprimirCabecalho("GERACAO DE TIPOS A PARTIR DO OPENAPI");
    escreverLinha(`Entrada: ${pc.dim(entrada)}`);
    escreverLinha(`Saida: ${pc.dim(saida)}`);

    const resultado = await gerarTiposContrato({entrada, saida, url});
    escreverLinha(pc.green("Tipos gerados com sucesso."));

    if (emitirJson) {
        imprimirJson(resultado);
    }
}

main().catch((erro) => {
    escreverLinha(pc.red(`Erro ao gerar tipos de contrato: ${erro instanceof Error ? erro.message : String(erro)}`));
    process.exit(1);
});

export {
    gerarTiposContrato
};
