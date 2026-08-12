#!/usr/bin/env node

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterLinhas, obterOpcoesCdu} from "./cdus-lib.js";
import {extrairAssuntos, extrairDescricoes, extrairMensagens, extrairToasts} from "./cdus-mensagens-lib.js";

function adicionarAchado(achados, severidade, regra, mensagem, linha = null) {
    achados.push({severidade, regra, mensagem, linha});
}

function localizarLinha(linhas, trecho) {
    const indice = linhas.findIndex(linha => linha.includes(trecho));
    return indice >= 0 ? indice + 1 : null;
}

function auditarArquivo(caminhoArquivo) {
    const texto = lerArquivo(caminhoArquivo);
    const linhas = obterLinhas(texto);
    const achados = [];

    for (const descricao of extrairDescricoes(texto)) {
        if (/\s{2,}/.test(descricao)) {
            adicionarAchado(
                achados,
                "aviso",
                "descricao_espacamento",
                `Descrição com espaçamento suspeito: "${descricao}".`,
                localizarLinha(linhas, descricao)
            );
        }
        if (/\[\w/.test(descricao)) {
            adicionarAchado(
                achados,
                "aviso",
                "descricao_placeholder_legado",
                `Descrição ainda contém placeholder legado: "${descricao}".`,
                localizarLinha(linhas, descricao)
            );
        }
    }

    for (const assunto of extrairAssuntos(texto)) {
        if (assunto.endsWith(']')) {
            adicionarAchado(
                achados,
                "aviso",
                "assunto_fechamento_suspeito",
                `Assunto com fechamento suspeito: "${assunto}".`,
                localizarLinha(linhas, assunto)
            );
        }
        if (/\[\w/.test(assunto)) {
            adicionarAchado(
                achados,
                "aviso",
                "assunto_placeholder_legado",
                `Assunto ainda contém placeholder legado: "${assunto}".`,
                localizarLinha(linhas, assunto)
            );
        }
    }

    for (const mensagem of extrairMensagens(texto)) {
        if (/\[\w/.test(mensagem)) {
            adicionarAchado(
                achados,
                "aviso",
                "mensagem_placeholder_legado",
                `Mensagem ainda contém placeholder legado: "${mensagem}".`,
                localizarLinha(linhas, mensagem)
            );
        }
    }

    for (const toast of extrairToasts(texto)) {
        if (/\[\w/.test(toast)) {
            adicionarAchado(
                achados,
                "aviso",
                "toast_placeholder_legado",
                `Toast ainda contém placeholder legado: "${toast}".`,
                localizarLinha(linhas, toast)
            );
        }
    }

    return achados;
}

async function principal(argumentos = process.argv.slice(2)) {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

    const arquivos = await listarArquivosCdu(base);
    const relatorio = arquivos.map(caminhoArquivo => ({
        arquivo: path.relative(base, caminhoArquivo).replaceAll("\\", "/"),
        achados: auditarArquivo(caminhoArquivo)
    }));

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

    escreverLinha(`Auditoria de mensagens dos CDUs em ${path.join(base, "specs")}`);
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
