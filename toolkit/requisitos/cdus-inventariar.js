#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {
    analisarArquivo,
    extrairCabecalhoFluxo,
    extrairCabecalhoPre,
    extrairLinhaAtor,
    lerArquivo,
    listarArquivosCdu
} from "./cdus-lib.js";

function acumularMapa(mapa, chave) {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa) {
    return Object.fromEntries(
        Object.entries(mapa).sort((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();

const arquivos = await listarArquivosCdu(base);
const inventario = {
    base,
    totalArquivos: arquivos.length,
    formatosAtor: {},
    formatosPreCondicoes: {},
    formatosFluxoPrincipal: {},
    documentosComReinicioNumeracao: [],
    documentosComRegressaoNumeracao: [],
    situacoesMaisFrequentes: {},
    elementosUiMaisFrequentes: {},
    placeholdersMaisFrequentes: {}
};

for (const caminhoArquivo of arquivos) {
    const texto = lerArquivo(caminhoArquivo);
    const analise = analisarArquivo(caminhoArquivo, texto);

    acumularMapa(inventario.formatosAtor, extrairLinhaAtor(texto) ?? "<ausente>");
    acumularMapa(inventario.formatosPreCondicoes, extrairCabecalhoPre(texto) ?? "<ausente>");
    acumularMapa(inventario.formatosFluxoPrincipal, extrairCabecalhoFluxo(texto) ?? "<ausente>");

    if (analise.repeticoes.length > 0) {
        inventario.documentosComReinicioNumeracao.push({
            arquivo: analise.nomeArquivo,
            repeticoes: analise.repeticoes
        });
    }

    if (analise.regressoes.length > 0) {
        inventario.documentosComRegressaoNumeracao.push({
            arquivo: analise.nomeArquivo,
            regressoes: analise.regressoes
        });
    }

    for (const situacao of texto.match(/'[^'\n]+'/g) ?? []) {
        acumularMapa(inventario.situacoesMaisFrequentes, situacao);
    }

    for (const elementoUi of texto.match(/`[^`\n]+`/g) ?? []) {
        acumularMapa(inventario.elementosUiMaisFrequentes, elementoUi);
    }

    for (const placeholder of texto.match(/\[[A-Z0-9_]+\]/g) ?? []) {
        acumularMapa(inventario.placeholdersMaisFrequentes, placeholder);
    }
}

inventario.formatosAtor = ordenarMapa(inventario.formatosAtor);
inventario.formatosPreCondicoes = ordenarMapa(inventario.formatosPreCondicoes);
inventario.formatosFluxoPrincipal = ordenarMapa(inventario.formatosFluxoPrincipal);
inventario.situacoesMaisFrequentes = ordenarMapa(inventario.situacoesMaisFrequentes);
inventario.elementosUiMaisFrequentes = ordenarMapa(inventario.elementosUiMaisFrequentes);
inventario.placeholdersMaisFrequentes = ordenarMapa(inventario.placeholdersMaisFrequentes);

if (emitirJson) {
    imprimirJson(inventario);
    process.exit(0);
}

console.log(`Inventário dos CDUs em ${path.join(base, "specs")}`);
console.log(`Arquivos analisados: ${inventario.totalArquivos}`);
console.log("");
console.log("Formatos de ator:");
for (const [formato, quantidade] of Object.entries(inventario.formatosAtor)) {
    console.log(`- ${quantidade}x ${formato}`);
}
console.log("");
console.log("Formatos de pré-condições:");
for (const [formato, quantidade] of Object.entries(inventario.formatosPreCondicoes)) {
    console.log(`- ${quantidade}x ${formato}`);
}
console.log("");
console.log("Formatos de fluxo principal:");
for (const [formato, quantidade] of Object.entries(inventario.formatosFluxoPrincipal)) {
    console.log(`- ${quantidade}x ${formato}`);
}
console.log("");
console.log(`Documentos com repetição de numeração: ${inventario.documentosComReinicioNumeracao.length}`);
console.log(`Documentos com regressão de numeração: ${inventario.documentosComRegressaoNumeracao.length}`);
