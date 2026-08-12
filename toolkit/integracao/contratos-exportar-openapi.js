#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_LATEST, URL_OPENAPI_PADRAO} from "./contratos-openapi-caminhos.js";

async function exportarOpenapi({url = URL_OPENAPI_PADRAO, saida = CAMINHO_OPENAPI_LATEST}) {
    const resposta = await fetch(url, {
        headers: {
            Accept: "application/json"
        }
    });

    if (!resposta.ok) {
        throw new Error(`Falha ao buscar OpenAPI em ${url}: HTTP ${resposta.status}`);
    }

    const json = await resposta.json();
    await fs.mkdir(path.dirname(saida), {recursive: true});
    await fs.writeFile(saida, `${JSON.stringify(json, null, 2)}\n`, "utf-8");

    return {
        url,
        saida,
        titulo: json.info?.title ?? null,
        versao: json.info?.version ?? null,
        paths: Object.keys(json.paths ?? {}).length
    };
}

async function principal(argumentos = process.argv.slice(2)) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos exportar-openapi",
            scriptDireto: "integracao/contratos-exportar-openapi.js",
            descricao: "Busca o documento OpenAPI da aplicação em execução e grava uma fotografia local para auditorias de contrato.",
            opcoes: [
                "--url <url>          URL do endpoint OpenAPI (padrão: http://127.0.0.1:10000/api-docs).",
                "--saida <arquivo>    Caminho do arquivo JSON a ser gerado.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.js integracao contratos exportar-openapi",
                "npx tsx toolkit/sgc.js integracao contratos exportar-openapi --url http://127.0.0.1:10000/api-docs",
                "npx tsx toolkit/sgc.js integracao contratos exportar-openapi --saida /tmp/sgc-openapi.json --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const url = lerOpcao(argumentos, "--url", URL_OPENAPI_PADRAO);
    const saida = lerOpcao(argumentos, "--saida", CAMINHO_OPENAPI_LATEST);

    if (!emitirJson) {
        imprimirCabecalho("EXPORTACAO DO OPENAPI");
        escreverLinha(`Origem: ${pc.dim(url)}`);
        escreverLinha(`Saida: ${pc.dim(saida)}`);
    }

    try {
        const resultado = await exportarOpenapi({url, saida});
        if (emitirJson) {
            imprimirJson(resultado);
        } else {
            escreverLinha(pc.green(`OpenAPI exportado com sucesso.`));
            escreverLinha(`Titulo: ${resultado.titulo ?? "-"}`);
            escreverLinha(`Versao: ${resultado.versao ?? "-"}`);
            escreverLinha(`Paths: ${resultado.paths}`);
        }
    } catch (erro) {
        process.stderr.write(`Erro ao exportar OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.stderr.write("Dica: execute o backend com perfil `e2e` ou informe `--url` para uma instância já ativa.\n");
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    exportarOpenapi,
    principal
};
