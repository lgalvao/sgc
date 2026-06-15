#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import {pathToFileURL} from "node:url";
import pc from "picocolors";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_LATEST, URL_OPENAPI_PADRAO} from "./contratos-openapi-caminhos.js";

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

async function main() {
    const args = process.argv.slice(2);
    if (args.includes("--help") || args.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos exportar-openapi",
            scriptDireto: "integracao/contratos-exportar-openapi.js",
            descricao: "Busca o documento OpenAPI da aplicação em execução e grava um snapshot local para auditorias de contrato.",
            opcoes: [
                "--url <url>          URL do endpoint OpenAPI (padrão: http://127.0.0.1:10000/api-docs).",
                "--saida <arquivo>    Caminho do arquivo JSON a ser gerado.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "node toolkit/sgc.js integracao contratos exportar-openapi",
                "node toolkit/sgc.js integracao contratos exportar-openapi --url http://127.0.0.1:10000/api-docs",
                "node toolkit/sgc.js integracao contratos exportar-openapi --saida /tmp/sgc-openapi.json --json"
            ]
        });
        return;
    }

    const emitirJson = args.includes("--json");
    const url = lerOpcao(args, "--url", URL_OPENAPI_PADRAO);
    const saida = lerOpcao(args, "--saida", CAMINHO_OPENAPI_LATEST);

    imprimirCabecalho("EXPORTACAO DO OPENAPI");
    escreverLinha(`Origem: ${pc.dim(url)}`);
    escreverLinha(`Saida: ${pc.dim(saida)}`);

    try {
        const resultado = await exportarOpenapi({url, saida});
        escreverLinha(pc.green(`OpenAPI exportado com sucesso.`));
        escreverLinha(`Titulo: ${resultado.titulo ?? "-"}`);
        escreverLinha(`Versao: ${resultado.versao ?? "-"}`);
        escreverLinha(`Paths: ${resultado.paths}`);

        if (emitirJson) {
            imprimirJson(resultado);
        }
    } catch (erro) {
        escreverLinha(pc.red(`Erro ao exportar OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}`));
        escreverLinha("Dica: execute o backend com perfil `e2e` ou informe `--url` para uma instância já ativa.");
        process.exit(1);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main();
}

export {
    exportarOpenapi
};
