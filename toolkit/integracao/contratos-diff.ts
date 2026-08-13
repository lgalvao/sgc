
import fs from "node:fs/promises";
import path from "node:path";
import {execa} from "execa";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../biblioteca/caminhos.js";
import {exibirAjudaComando} from "../biblioteca/cli-ajuda.js";
import {lerOpcao} from "../biblioteca/cli-opcoes.js";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../biblioteca/saida.js";
import {resolverCaminhoArquivoOpenapi, resolverCaminhosOpenapi} from "./contratos-openapi-caminhos.js";

interface OpcoesDiffContratos {
    base?: string;
    anterior?: string;
    atual?: string;
}

type ModoDiffContratos = "identico" | "diferencaTextual";

interface ResultadoDiffContratos {
    base: string;
    anterior: string;
    atual: string;
    houveMudancas: boolean;
    modo: ModoDiffContratos;
    saidaPadrao: string;
    saidaErro: string;
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
            houveMudancas: false,
            modo: "identico",
            saidaPadrao: "Nenhuma diferença detectada entre a referência e a fotografia atual.",
            saidaErro: ""
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
        houveMudancas: true,
        modo: "diferencaTextual",
        saidaPadrao: resultado.stdout ?? "",
        saidaErro: resultado.stderr ?? ""
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
        resultado.saidaPadrao || resultado.saidaErro || "Sem saída.",
        "```",
        ""
    ].join("\n");
}

async function principal(argumentosInformados: string[] = process.argv.slice(2)): Promise<void> {
    const argumentos = validarArgumentosEntradaDireta(import.meta.url, argumentosInformados);
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
    const anterior = resolverCaminhoArquivoOpenapi(
        base,
        lerOpcao(argumentos, "--anterior", caminhos.caminhoReferencia) ?? caminhos.caminhoReferencia
    );
    const atual = resolverCaminhoArquivoOpenapi(
        base,
        lerOpcao(argumentos, "--atual", caminhos.caminhoAtual) ?? caminhos.caminhoAtual
    );

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
    } else if (resultado.saidaPadrao) {
        escreverLinha(resultado.saidaPadrao);
    } else if (resultado.saidaErro) {
        escreverLinha(resultado.saidaErro);
    } else {
        escreverLinha("Nenhuma diferença textual reportada pelo git diff.");
    }

}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        escreverErro(`Erro ao comparar contratos OpenAPI: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    });
}

export {
    executarDiffContratos,
    principal
};
