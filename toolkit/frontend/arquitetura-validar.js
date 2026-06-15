#!/usr/bin/env node
import path from "node:path";
import {createRequire} from "node:module";
import {pathToFileURL} from "node:url";
import {realpathSync} from "node:fs";
import pc from "picocolors";
import {cruise} from "dependency-cruiser";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";

function lerOpcao(argumentos, nome) {
    const indice = argumentos.indexOf(nome);
    if (indice === -1) {
        return null;
    }
    return argumentos[indice + 1] ?? null;
}

function carregarRegras(caminhoConfiguracao) {
    const require = createRequire(import.meta.url);
    delete require.cache[caminhoConfiguracao];
    return require(caminhoConfiguracao);
}

function extrairViolacoes(parsed) {
    if (Array.isArray(parsed?.summary?.violations)) {
        return parsed.summary.violations;
    }
    if (Array.isArray(parsed?.summary?.error)) {
        return parsed.summary.error;
    }
    return [];
}

function normalizarSaidaCaminho(caminhoArquivo, diretorioBase) {
    if (!caminhoArquivo) {
        return "(desconhecido)";
    }
    if (!path.isAbsolute(caminhoArquivo)) {
        return caminhoArquivo.replaceAll("\\", "/");
    }
    return path.relative(diretorioBase, caminhoArquivo).replaceAll("\\", "/");
}

function formatarViolacao(violacao, diretorioBase) {
    const origem = normalizarSaidaCaminho(violacao.from, diretorioBase);
    const destinoBruto = violacao.to?.resolved ?? violacao.to ?? "(destino desconhecido)";
    const destino = typeof destinoBruto === "string"
        ? normalizarSaidaCaminho(destinoBruto, diretorioBase)
        : JSON.stringify(destinoBruto);
    const regra = violacao.rule?.name ?? violacao.name ?? "regra-desconhecida";
    const comentario = violacao.rule?.comment ?? violacao.comment ?? "Violacao arquitetural detectada.";
    return {origem, destino, regra, comentario};
}

function imprimirViolacoes(violacoes, diretorioBase) {
    if (violacoes.length === 0) {
        escreverLinha(`${pc.green("✓")} Nenhuma violacao arquitetural encontrada.`);
        return;
    }

    escreverLinha(pc.red(`Foram encontradas ${violacoes.length} violacoes arquiteturais:`));
    violacoes.forEach((violacao, indice) => {
        const item = formatarViolacao(violacao, diretorioBase);
        escreverLinha(`${indice + 1}. [${item.regra}] ${item.origem} -> ${item.destino}`);
        escreverLinha(`   ${item.comentario}`);
    });
}

async function executarValidacaoArquiteturaFrontend(opcoes = {}) {
    const diretorioBase = realpathSync(path.resolve(opcoes.base ?? DIRETORIO_RAIZ));
    const diretorioFrontend = realpathSync(path.join(diretorioBase, "frontend"));
    const caminhoSrc = "src";
    const caminhoConfiguracao = path.join(diretorioFrontend, ".dependency-cruiser.cjs");
    const regrasBase = carregarRegras(caminhoConfiguracao);
    const regras = {
        ...regrasBase,
        options: {
            ...regrasBase.options,
            tsConfig: {
                fileName: path.join(diretorioFrontend, "tsconfig.json"),
            },
        },
    };

    const resultadoCruise = await cruise(
        [caminhoSrc],
        {
            validate: true,
            ruleSet: regras,
            outputType: "json",
            cwd: diretorioFrontend,
            baseDir: diretorioFrontend,
        },
    );

    const parsed = JSON.parse(resultadoCruise.output);
    return {
        ...parsed,
        exitCode: resultadoCruise.exitCode,
        summary: {
            ...parsed.summary,
            violations: extrairViolacoes(parsed),
        },
    };
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend arquitetura validar",
            scriptDireto: "frontend/arquitetura-validar.js",
            descricao: "Valida regras arquiteturais duras do frontend usando resolucao real de modulos.",
            opcoes: [
                "--json               Emite o resultado bruto em JSON.",
                "--base <diretorio>   Sobrescreve o diretorio base da validacao.",
            ],
            exemplos: [
                "node toolkit/sgc.js frontend arquitetura validar",
                "node toolkit/sgc.js frontend arquitetura validar --json",
                "node toolkit/sgc.js frontend arquitetura validar --base C:/sgc",
            ],
        });
        process.exit(0);
    }

    const resultado = await executarValidacaoArquiteturaFrontend({
        base: lerOpcao(args, "--base"),
    });

    if (jsonMode) {
        imprimirJson(resultado);
    } else {
        imprimirViolacoes(resultado.summary.violations ?? [], path.resolve(lerOpcao(args, "--base") ?? DIRETORIO_RAIZ));
    }

    if ((resultado.summary.violations ?? []).length > 0 || resultado.exitCode !== 0) {
        process.exit(1);
    }
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
    main().catch((erro) => {
        escreverLinha(pc.red(`Erro na validacao arquitetural: ${erro.message}`));
        process.exit(1);
    });
}

export {
    executarValidacaoArquiteturaFrontend,
};
