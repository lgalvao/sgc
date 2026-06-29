#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {listarArquivosCdu, lerArquivo} from "./cdus-lib.js";
import {
    acumularMapa,
    extrairAssuntos,
    extrairDescricoes,
    extrairMensagens,
    extrairToasts,
    ordenarMapa
} from "./cdus-mensagens-lib.js";

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();

const arquivos = await listarArquivosCdu(base);
const inventario = {
    base,
    totalArquivos: arquivos.length,
    descricoes: {},
    assuntos: {},
    mensagens: {},
    toasts: {}
};

for (const caminhoArquivo of arquivos) {
    const texto = lerArquivo(caminhoArquivo);
    for (const descricao of extrairDescricoes(texto)) {
        acumularMapa(inventario.descricoes, descricao);
    }
    for (const assunto of extrairAssuntos(texto)) {
        acumularMapa(inventario.assuntos, assunto);
    }
    for (const mensagem of extrairMensagens(texto)) {
        acumularMapa(inventario.mensagens, mensagem);
    }
    for (const toast of extrairToasts(texto)) {
        acumularMapa(inventario.toasts, toast);
    }
}

inventario.descricoes = ordenarMapa(inventario.descricoes);
inventario.assuntos = ordenarMapa(inventario.assuntos);
inventario.mensagens = ordenarMapa(inventario.mensagens);
inventario.toasts = ordenarMapa(inventario.toasts);

if (emitirJson) {
    imprimirJson(inventario);
    process.exit(0);
}

console.log(`Inventário de mensagens dos CDUs em ${path.join(base, "specs")}`);
console.log(`Arquivos analisados: ${inventario.totalArquivos}`);
console.log("");
for (const chave of ["descricoes", "assuntos", "mensagens", "toasts"]) {
    console.log(`${chave}:`);
    for (const [valor, quantidade] of Object.entries(inventario[chave]).slice(0, 40)) {
        console.log(`- ${quantidade}x ${valor}`);
    }
    console.log("");
}
