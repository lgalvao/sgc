
import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {resolverCaminhoArquivoOpenapi, resolverCaminhosOpenapi} from "./contratos-openapi-caminhos.js";

interface OpcoesFixarBaselineContrato {
    base?: string;
    origem?: string;
    destino?: string;
}

interface ResultadoFixacaoBaselineContrato {
    base: string;
    origem: string;
    destino: string;
}

async function fixarBaselineContrato({base = DIRETORIO_RAIZ, origem, destino}: OpcoesFixarBaselineContrato = {}): Promise<ResultadoFixacaoBaselineContrato> {
    const caminhos = resolverCaminhosOpenapi(base);
    const origemResolvida = resolverCaminhoArquivoOpenapi(base, origem ?? caminhos.caminhoAtual);
    const destinoResolvido = resolverCaminhoArquivoOpenapi(base, destino ?? caminhos.caminhoReferencia);
    await fs.mkdir(path.dirname(destinoResolvido), {recursive: true});
    await fs.copyFile(origemResolvida, destinoResolvido);
    return {base: caminhos.base, origem: origemResolvida, destino: destinoResolvido};
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
    if (argumentos.includes("--help") || argumentos.includes("-h")) {
        exibirAjudaComando({
            comandoToolkit: "integracao contratos fixar-baseline",
            scriptDireto: "integracao/contratos-fixar-baseline.ts",
            descricao: "Promove a fotografia OpenAPI mais recente como referência para comparações futuras de contrato.",
            opcoes: [
                "--base <diretorio>   Base do projeto que contém os artefatos padrão.",
                "--origem <arquivo>   Fotografia OpenAPI atual.",
                "--destino <arquivo>  Referência a ser atualizada.",
                "--json               Emite o resultado em JSON."
            ],
            exemplos: [
                "npx tsx toolkit/ferramentas.ts integracao contratos fixar-baseline",
                "npx tsx toolkit/ferramentas.ts integracao contratos fixar-baseline --origem /tmp/novo.json",
                "npx tsx toolkit/ferramentas.ts integracao contratos fixar-baseline --json"
            ]
        });
        return;
    }

    const emitirJson = argumentos.includes("--json");
    const base = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ) ?? DIRETORIO_RAIZ);
    const caminhos = resolverCaminhosOpenapi(base);
    const origem = lerOpcao(argumentos, "--origem", caminhos.caminhoAtual) ?? caminhos.caminhoAtual;
    const destino = lerOpcao(argumentos, "--destino", caminhos.caminhoReferencia) ?? caminhos.caminhoReferencia;

    if (!emitirJson) {
        imprimirCabecalho("FIXAR BASELINE DO OPENAPI");
        escreverLinha(`Origem: ${pc.dim(origem)}`);
        escreverLinha(`Destino: ${pc.dim(destino)}`);
    }

    const resultado = await fixarBaselineContrato({base, origem, destino});

    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        escreverLinha(pc.green("Baseline atualizada com sucesso."));
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        escreverErro(`Erro ao fixar baseline OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    fixarBaselineContrato,
    principal
};
