#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu} from "./cdus-lib.js";
import {extrairAssuntos, extrairDescricoes, extrairMensagens, extrairToasts} from "./cdus-mensagens-lib.js";

function normalizarBloco(texto) {
    return texto
        .trim()
        .replaceAll(/\r/g, "")
        .replaceAll(/[ \t]+/g, " ")
        .replaceAll(/\n{2,}/g, "\n");
}

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();

const arquivos = await listarArquivosCdu(base);
const blocos = new Map();
const itens = new Map();

function registrar(mapa, chave, arquivo, tipo, amostra) {
    const atual = mapa.get(chave) ?? {arquivos: [], tipo, amostra};
    atual.arquivos.push(arquivo);
    mapa.set(chave, atual);
}

for (const caminhoArquivo of arquivos) {
    const texto = lerArquivo(caminhoArquivo);
    const arquivoRelativo = path.relative(base, caminhoArquivo).replaceAll("\\", "/");
    for (const match of texto.matchAll(/```text\n([\s\S]*?)```/g)) {
        const bloco = normalizarBloco(match[1]);
        if (bloco.length < 40) {
            continue;
        }
        registrar(blocos, bloco, arquivoRelativo, "bloco_texto", bloco.split("\n").slice(0, 6).join("\n"));
    }

    for (const assunto of extrairAssuntos(texto)) {
        registrar(itens, `assunto:${assunto}`, arquivoRelativo, "assunto", assunto);
    }

    for (const descricao of extrairDescricoes(texto)) {
        registrar(itens, `descricao:${descricao}`, arquivoRelativo, "descricao", descricao);
    }

    for (const mensagem of extrairMensagens(texto)) {
        registrar(itens, `mensagem:${mensagem}`, arquivoRelativo, "mensagem", mensagem);
    }

    for (const toast of extrairToasts(texto)) {
        registrar(itens, `toast:${toast}`, arquivoRelativo, "toast", toast);
    }
}

const duplicacoes = [
    ...[...blocos.values()].map(item => ({
        ocorrencias: item.arquivos.length,
        arquivos: [...new Set(item.arquivos)].sort((a, b) => a.localeCompare(b, "pt-BR")),
        tipo: item.tipo,
        amostra: item.amostra
    })),
    ...[...itens.values()].map(item => ({
        ocorrencias: item.arquivos.length,
        arquivos: [...new Set(item.arquivos)].sort((a, b) => a.localeCompare(b, "pt-BR")),
        tipo: item.tipo,
        amostra: item.amostra
    }))
]
    .filter(item => item.arquivos.length > 1)
    .sort((a, b) => b.ocorrencias - a.ocorrencias || a.arquivos[0].localeCompare(b.arquivos[0], "pt-BR"));

const resultado = {
    base,
    totalArquivos: arquivos.length,
    duplicacoes
};

if (emitirJson) {
    imprimirJson(resultado);
    process.exit(0);
}

console.log(`Inventário de duplicações dos CDUs em ${path.join(base, "specs")}`);
console.log(`Arquivos analisados: ${resultado.totalArquivos}`);
console.log(`Blocos duplicados: ${resultado.duplicacoes.length}`);
console.log("");

for (const duplicacao of resultado.duplicacoes.slice(0, 20)) {
    console.log(`${duplicacao.ocorrencias}x [${duplicacao.tipo}] ${duplicacao.arquivos.join(", ")}`);
    console.log(duplicacao.amostra);
    console.log("");
}
