#!/usr/bin/env node

import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {CAMINHO_OPENAPI_BASELINE, CAMINHO_OPENAPI_LATEST} from "./contratos-openapi-caminhos.js";

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

async function fixarBaselineContrato({origem = CAMINHO_OPENAPI_LATEST, destino = CAMINHO_OPENAPI_BASELINE}) {
    await fs.mkdir(path.dirname(destino), {recursive: true});
    await fs.copyFile(origem, destino);
    return {origem, destino};
}

async function main() {
    const args = process.argv.slice(2);
    if (args.includes("--help") || args.includes("-h")) {
        exibirAjudaComando({
            comandoSgc: "integracao contratos fixar-baseline",
            scriptDireto: "integracao/contratos-fixar-baseline.js",
            descricao: "Promove o snapshot OpenAPI mais recente como baseline para comparações futuras de contrato.",
            opcoes: [
                "--origem <arquivo>   Snapshot OpenAPI atual.",
                "--destino <arquivo>  Baseline a ser atualizada.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "node etc/scripts/sgc.js integracao contratos fixar-baseline",
                "node etc/scripts/sgc.js integracao contratos fixar-baseline --origem /tmp/novo.json",
                "node etc/scripts/sgc.js integracao contratos fixar-baseline --json"
            ]
        });
        return;
    }

    const emitirJson = args.includes("--json");
    const origem = lerOpcao(args, "--origem", CAMINHO_OPENAPI_LATEST);
    const destino = lerOpcao(args, "--destino", CAMINHO_OPENAPI_BASELINE);

    imprimirCabecalho("FIXAR BASELINE DO OPENAPI");
    escreverLinha(`Origem: ${pc.dim(origem)}`);
    escreverLinha(`Destino: ${pc.dim(destino)}`);

    const resultado = await fixarBaselineContrato({origem, destino});
    escreverLinha(pc.green("Baseline atualizada com sucesso."));

    if (emitirJson) {
        imprimirJson(resultado);
    }
}

main().catch((erro) => {
    escreverLinha(pc.red(`Erro ao fixar baseline OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}`));
    process.exit(1);
});

export {
    fixarBaselineContrato
};
