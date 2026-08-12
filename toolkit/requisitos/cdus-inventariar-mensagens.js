#!/usr/bin/env node

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {
    acumularMapa,
    extrairAssuntos,
    extrairDescricoes,
    extrairMensagens,
    extrairToasts,
    ordenarMapa
} from "./cdus-mensagens-lib.js";

async function principal(argumentos = process.argv.slice(2)) {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

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
        return;
    }

    escreverLinha(`Inventário de mensagens dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${inventario.totalArquivos}`);
    escreverLinha();
    for (const chave of ["descricoes", "assuntos", "mensagens", "toasts"]) {
        escreverLinha(`${chave}:`);
        for (const [valor, quantidade] of Object.entries(inventario[chave]).slice(0, 40)) {
            escreverLinha(`- ${quantidade}x ${valor}`);
        }
        escreverLinha();
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
