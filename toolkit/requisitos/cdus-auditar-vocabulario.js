#!/usr/bin/env node

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {
    carregarSituacoesCanonicas,
    PERFIS_CANONICOS,
    sugerirCanonico,
    TIPOS_PROCESSO_CANONICOS
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

async function principal(argumentos = process.argv.slice(2)) {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);
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
        return;
    }

    escreverLinha(`Auditoria de vocabulário dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resumo.totalArquivos}`);
    escreverLinha(`Arquivos com aviso: ${resumo.arquivosComAviso}`);
    escreverLinha(`Avisos: ${resumo.avisos}`);
    escreverLinha();

    for (const item of relatorio.filter(entrada => entrada.achados.length > 0)) {
        escreverLinha(item.arquivo);
        for (const achado of item.achados) {
            const sufixoLinha = achado.linha ? ` (linha ${achado.linha})` : "";
            escreverLinha(`- [${achado.severidade}] ${achado.regra}${sufixoLinha}: ${achado.mensagem}`);
        }
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
