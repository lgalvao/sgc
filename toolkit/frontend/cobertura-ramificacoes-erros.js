#!/usr/bin/env node
import fs from "node:fs/promises";
import path from "node:path";
import pc from "picocolors";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";

const PADROES_SUSPEITOS = [
    /\bcatch\s*\(/,
    /\bnormalizarErro\s*\(/,
    /\bnotify\s*\(/,
    /\bdeveNotificarGlobalmente\s*\(/,
    /\bapp\.config\.errorHandler\b/,
    /\bultimoErro\b/,
    /\berro[A-Z_a-zA-Z0-9]*\s*=/,
    /\bPromise\.reject\b/,
];

function calcularBranchesPerdidos(arquivo) {
    return Math.max(0, arquivo.branchesTotal - Math.round((arquivo.branchesPercentual / 100) * arquivo.branchesTotal));
}

async function coletarLinhasSuspeitas(caminhoRelativo, diretorioBase) {
    const caminhoAbsoluto = path.resolve(diretorioBase, caminhoRelativo);
    const conteudo = await fs.readFile(caminhoAbsoluto, "utf8");
    const linhas = conteudo.split(/\r?\n/);
    return linhas
        .map((linha, indice) => ({numero: indice + 1, texto: linha.trim()}))
        .filter(({texto}) => PADROES_SUSPEITOS.some((padrao) => padrao.test(texto)))
        .slice(0, 12);
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend cobertura ramificacoes-erros",
            scriptDireto: "frontend/cobertura-ramificacoes-erros.js",
            descricao: "Cruza lacunas de ramificacoes do frontend com sinais de tratamento de erro suspeito.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite <n>    Limita a quantidade de arquivos inspecionados. Padrão: 15.",
                "--arquivo <arquivo> Usa um relatório V8 específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const caminhoRelatorio = lerOpcao(argumentos, "--arquivo", undefined);
    const limite = Number.parseInt(lerOpcao(argumentos, "--limite", "15"), 10);
    const coleta = await extrairCoberturaFrontend(caminhoRelatorio, {diretorioBase});
    const candidatos = coleta.arquivos
        .map((arquivo) => ({
            ...arquivo,
            branchesPerdidos: calcularBranchesPerdidos(arquivo),
        }))
        .filter((arquivo) => arquivo.branchesPerdidos > 0)
        .toSorted((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite);

    const arquivos = [];
    for (const candidato of candidatos) {
        const linhasSuspeitas = await coletarLinhasSuspeitas(candidato.arquivo, diretorioBase);
        if (linhasSuspeitas.length === 0) {
            continue;
        }
        arquivos.push({
            arquivo: candidato.arquivo,
            branchesPerdidos: candidato.branchesPerdidos,
            branchesTotal: candidato.branchesTotal,
            branchesPercentual: candidato.branchesPercentual,
            linhasSuspeitas,
        });
    }

    const resultado = {
        status: "ok",
        timestamp: new Date().toISOString(),
        totais: coleta.branches,
        arquivos,
    };

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("BRANCHES DE ERRO SUSPEITOS NO FRONTEND");
    escreverLinha(`Cobertura global de branches: ${pc.bold(`${coleta.branches.percentual}%`)} (${coleta.branches.cobertos}/${coleta.branches.total})`);
    escreverLinha("");

    if (arquivos.length === 0) {
        escreverLinha(pc.green("Nenhum hotspot com sinais claros de tratamento de erro foi encontrado no recorte atual."));
        return;
    }

    arquivos.forEach((arquivo, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(arquivo.arquivo)}`);
        escreverLinha(`   Branches perdidos: ${arquivo.branchesPerdidos}/${arquivo.branchesTotal} | Cobertura: ${arquivo.branchesPercentual}%`);
        arquivo.linhasSuspeitas.forEach((linha) => {
            escreverLinha(`   L${linha.numero}: ${pc.dim(linha.texto)}`);
        });
        escreverLinha("");
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao cruzar branches de erro suspeitos: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
