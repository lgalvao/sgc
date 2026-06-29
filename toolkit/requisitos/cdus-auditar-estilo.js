#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {listarArquivosCdu, lerArquivo, obterLinhas} from "./cdus-lib.js";

const REGEX_ASPAS_SIMPLES = /'([^'\n]+)'/g;
const REGEX_TITULO_UI_EM_ASPAS = /(?:título|titulo|subtítulo|subtitulo)\s*:?\s*"([^"\n]+)"/gi;
const REGEX_PLACEHOLDER_LEGADO = /\[[A-Z0-9_]+\]/g;
const PERFIS = new Set(["ADMIN", "GESTOR", "CHEFE", "SERVIDOR", "TODOS"]);

function adicionarAchado(achados, severidade, regra, mensagem, linha = null) {
    achados.push({severidade, regra, mensagem, linha});
}

function encontrarAspasSimplesSuspeitas(linhas, achados) {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_ASPAS_SIMPLES)) {
            const valor = correspondencia[1].trim();
            if (PERFIS.has(valor)) {
                adicionarAchado(
                    achados,
                    "aviso",
                    "perfil_em_aspas_simples",
                    `Perfil em aspas simples: '${valor}'. Use \`${valor}\`.`,
                    indice + 1
                );
            }
        }
    });
}

function encontrarUiEmAspasDuplas(linhas, achados) {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_TITULO_UI_EM_ASPAS)) {
            const valor = correspondencia[1].trim();
            const pareceMensagemLiteral = /[.?!]/.test(valor) || valor.split(/\s+/).length > 6;
            if (pareceMensagemLiteral) {
                continue;
            }

            adicionarAchado(
                achados,
                "aviso",
                "ui_em_aspas_duplas",
                `Possível título ou subtítulo de interface em aspas duplas: "${valor}". Considere usar crases.`,
                indice + 1
            );
        }
    });
}

function encontrarPlaceholdersLegados(linhas, achados) {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_PLACEHOLDER_LEGADO)) {
            const valor = correspondencia[0];
            adicionarAchado(
                achados,
                "aviso",
                "placeholder_legado",
                `Placeholder no formato legado: ${valor}. Prefira \`${valor.replaceAll("[", ":").replaceAll("]", ":")}\`.`,
                indice + 1
            );
        }
    });
}

function auditarArquivo(caminhoArquivo) {
    const texto = lerArquivo(caminhoArquivo);
    const linhas = obterLinhas(texto);
    const achados = [];

    encontrarAspasSimplesSuspeitas(linhas, achados);
    encontrarUiEmAspasDuplas(linhas, achados);
    encontrarPlaceholdersLegados(linhas, achados);

    return {
        arquivo: caminhoArquivo,
        achados
    };
}

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();

const arquivos = await listarArquivosCdu(base);
const relatorio = arquivos.map(caminhoArquivo => {
    const resultado = auditarArquivo(caminhoArquivo);
    return {
        arquivo: path.relative(base, resultado.arquivo).replaceAll("\\", "/"),
        achados: resultado.achados
    };
});

const resumo = {
    base,
    totalArquivos: relatorio.length,
    arquivosComAviso: relatorio.filter(item => item.achados.length > 0).length,
    avisos: relatorio.flatMap(item => item.achados).length
};

if (emitirJson) {
    imprimirJson({resumo, relatorio});
    process.exit(0);
}

console.log(`Auditoria tipográfica read-only dos CDUs em ${path.join(base, "specs")}`);
console.log(`Arquivos analisados: ${resumo.totalArquivos}`);
console.log(`Arquivos com aviso: ${resumo.arquivosComAviso}`);
console.log(`Avisos: ${resumo.avisos}`);
console.log("");

for (const item of relatorio.filter(entrada => entrada.achados.length > 0)) {
    console.log(item.arquivo);
    for (const achado of item.achados) {
        const sufixoLinha = achado.linha ? ` (linha ${achado.linha})` : "";
        console.log(`- [${achado.severidade}] ${achado.regra}${sufixoLinha}: ${achado.mensagem}`);
    }
}
