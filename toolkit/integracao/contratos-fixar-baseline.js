#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_BASELINE, CAMINHO_OPENAPI_LATEST} from "./contratos-openapi-caminhos.js";

async function fixarBaselineContrato({origem = CAMINHO_OPENAPI_LATEST, destino = CAMINHO_OPENAPI_BASELINE}) {
    await fs.mkdir(path.dirname(destino), {recursive: true});
    await fs.copyFile(origem, destino);
    return {origem, destino};
}

async function principal(argumentos = process.argv.slice(2)) {
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos fixar-baseline",
            scriptDireto: "integracao/contratos-fixar-baseline.js",
            descricao: "Promove a fotografia OpenAPI mais recente como referência para comparações futuras de contrato.",
            opcoes: [
                "--origem <arquivo>   Fotografia OpenAPI atual.",
                "--destino <arquivo>  Referência a ser atualizada.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "npx tsx toolkit/sgc.js integracao contratos fixar-baseline",
                "npx tsx toolkit/sgc.js integracao contratos fixar-baseline --origem /tmp/novo.json",
                "npx tsx toolkit/sgc.js integracao contratos fixar-baseline --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const origem = lerOpcao(argumentos, "--origem", CAMINHO_OPENAPI_LATEST);
    const destino = lerOpcao(argumentos, "--destino", CAMINHO_OPENAPI_BASELINE);

    if (!emitirJson) {
        imprimirCabecalho("FIXAR BASELINE DO OPENAPI");
        escreverLinha(`Origem: ${pc.dim(origem)}`);
        escreverLinha(`Destino: ${pc.dim(destino)}`);
    }

    const resultado = await fixarBaselineContrato({origem, destino});

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        escreverLinha(pc.green("Baseline atualizada com sucesso."));
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        process.stderr.write(`Erro ao fixar baseline OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    fixarBaselineContrato,
    principal
};
