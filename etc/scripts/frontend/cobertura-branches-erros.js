#!/usr/bin/env node
import fs from "node:fs/promises";
import pc from "picocolors";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {imprimirCabecalho, imprimirJson, escreverLinha} from "../lib/saida.js";
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

async function coletarLinhasSuspeitas(caminhoRelativo) {
    const caminhoAbsoluto = resolverNaRaiz(caminhoRelativo);
    const conteudo = await fs.readFile(caminhoAbsoluto, "utf8");
    const linhas = conteudo.split(/\r?\n/);
    return linhas
        .map((linha, indice) => ({numero: indice + 1, texto: linha.trim()}))
        .filter(({texto}) => PADROES_SUSPEITOS.some((padrao) => padrao.test(texto)))
        .slice(0, 12);
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");
    const limite = Number.parseInt(args.find((arg) => arg.startsWith("--limite="))?.split("=")[1] ?? "15", 10);

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend cobertura branches-erros",
            scriptDireto: "frontend/cobertura-branches-erros.js",
            descricao: "Cruza lacunas de branches do frontend com sinais de tratamento de erro suspeito.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite=N      Limita a quantidade de arquivos inspecionados. Padrão: 15."
            ]
        });
        process.exit(0);
    }

    const coleta = await extrairCoberturaFrontend();
    const candidatos = coleta.arquivos
        .map((arquivo) => ({
            ...arquivo,
            branchesPerdidos: calcularBranchesPerdidos(arquivo),
        }))
        .filter((arquivo) => arquivo.branchesPerdidos > 0)
        .sort((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite);

    const arquivos = [];
    for (const candidato of candidatos) {
        const linhasSuspeitas = await coletarLinhasSuspeitas(candidato.arquivo);
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

    if (jsonMode) {
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

main().catch((erro) => {
    console.error(pc.red(`Erro ao cruzar branches de erro suspeitos: ${erro.message}`));
    process.exit(1);
});
