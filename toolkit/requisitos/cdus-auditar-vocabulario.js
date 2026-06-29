#!/usr/bin/env node

import path from "node:path";
import {imprimirJson} from "../lib/saida.js";
import {listarArquivosCdu, lerArquivo} from "./cdus-lib.js";
import {
    PERFIS_CANONICOS,
    TIPOS_PROCESSO_CANONICOS,
    carregarSituacoesCanonicas,
    sugerirCanonico
} from "./cdus-vocabulario-lib.js";

function adicionarAchado(achados, severidade, regra, mensagem, linha = null) {
    achados.push({severidade, regra, mensagem, linha});
}

function auditarPerfis(texto, achados) {
    const linhas = texto.split(/\r?\n/);
    const indiceAtores = linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha));
    const indicePre = linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha));
    if (indiceAtores < 0 || indicePre < 0 || indicePre <= indiceAtores) {
        return;
    }

    linhas.slice(indiceAtores + 1, indicePre).forEach((linha, offset) => {
        if (!/^\s*-\s+/.test(linha)) {
            return;
        }

        const valor = linha.replace(/^\s*-\s+/, "").trim();
        if (PERFIS_CANONICOS.has(valor)) {
            return;
        }

        const sugestao = sugerirCanonico(valor, PERFIS_CANONICOS);
        const complemento = sugestao ? ` Sugestão: \`${sugestao}\`.` : "";
        adicionarAchado(
            achados,
            "aviso",
            "perfil_fora_vocabulario",
            `Perfil fora do vocabulário canônico: \`${valor}\`.${complemento}`,
            indiceAtores + offset + 2
        );
    });
}

function auditarSituacoesETipos(texto, achados, situacoesCanonicas) {
    const linhas = texto.split(/\r?\n/);
    linhas.forEach((linha, indice) => {
        const linhaNormalizada = linha.toLowerCase();
        const contextoSituacao = /situa[cç][aã]o|situa[cç][oõ]es|resultado/.test(linhaNormalizada);
        const contextoTipo = /tipo/.test(linhaNormalizada);

        for (const match of linha.matchAll(/'([^'\n]+)'/g)) {
            const valor = match[1].trim();
            const ehTipo = TIPOS_PROCESSO_CANONICOS.has(valor);
            const ehSituacao = situacoesCanonicas.has(valor);
            if (ehTipo || ehSituacao) {
                continue;
            }

            const sugestaoSituacao = contextoSituacao
                ? sugerirCanonico(valor, situacoesCanonicas)
                : null;
            const sugestaoTipo = contextoTipo
                ? sugerirCanonico(valor, TIPOS_PROCESSO_CANONICOS)
                : null;
            const sugestao = sugestaoSituacao ?? sugestaoTipo;
            if (!sugestao) {
                continue;
            }

            const regra = TIPOS_PROCESSO_CANONICOS.has(sugestao)
                ? "tipo_processo_variacao"
                : "situacao_variacao";
            const categoria = TIPOS_PROCESSO_CANONICOS.has(sugestao)
                ? "tipo de processo"
                : "situação";
            adicionarAchado(
                achados,
                "aviso",
                regra,
                `Possível variação de ${categoria}: '${valor}'. Sugestão: '${sugestao}'.`,
                indice + 1
            );
        }
    });
}

const args = process.argv.slice(2);
const emitirJson = args.includes("--json");
const indiceBase = args.indexOf("--base");
const base = indiceBase >= 0 ? path.resolve(args[indiceBase + 1]) : process.cwd();
const situacoesCanonicas = carregarSituacoesCanonicas(base);

const arquivos = await listarArquivosCdu(base);
const relatorio = arquivos.map(caminhoArquivo => {
    const texto = lerArquivo(caminhoArquivo);
    const achados = [];
    auditarPerfis(texto, achados);
    auditarSituacoesETipos(texto, achados, situacoesCanonicas);
    return {
        arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
        achados
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

console.log(`Auditoria de vocabulário dos CDUs em ${path.join(base, "specs")}`);
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
