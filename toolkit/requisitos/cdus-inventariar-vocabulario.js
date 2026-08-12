#!/usr/bin/env node

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {lerArquivo, listarArquivosCdu, obterOpcoesCdu} from "./cdus-lib.js";
import {carregarSituacoesCanonicas, PERFIS_CANONICOS, TIPOS_PROCESSO_CANONICOS} from "./cdus-vocabulario-lib.js";

function acumularMapa(mapa, chave) {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa) {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

function extrairItensListaAtores(texto) {
    const linhas = texto.split(/\r?\n/);
    const indiceAtores = linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha));
    const indicePre = linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha));
    if (indiceAtores < 0 || indicePre < 0 || indicePre <= indiceAtores) {
        return [];
    }

    return linhas
        .slice(indiceAtores + 1, indicePre)
        .filter(linha => /^\s*-\s+/.test(linha))
        .map(linha => linha.replace(/^\s*-\s+/, "").trim());
}

async function principal(argumentos = process.argv.slice(2)) {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);
    const situacoesCanonicas = carregarSituacoesCanonicas(base);

    const arquivos = await listarArquivosCdu(base);
    const inventario = {
        base,
        totalArquivos: arquivos.length,
        perfis: {},
        situacoes: {},
        tiposProcesso: {},
        elementosUi: {},
        canonicos: {
            perfis: [...PERFIS_CANONICOS],
            situacoes: [...situacoesCanonicas],
            tiposProcesso: [...TIPOS_PROCESSO_CANONICOS]
        }
    };

    for (const caminhoArquivo of arquivos) {
        const texto = lerArquivo(caminhoArquivo);

        for (const perfil of extrairItensListaAtores(texto)) {
            acumularMapa(inventario.perfis, perfil);
        }

        for (const match of texto.match(/'[^'\n]+'/g) ?? []) {
            acumularMapa(inventario.situacoes, match.slice(1, -1));
        }

        for (const tipo of texto.match(/'(Mapeamento|Revisão|Diagnóstico)'/g) ?? []) {
            acumularMapa(inventario.tiposProcesso, tipo.slice(1, -1));
        }

        for (const elementoUi of texto.match(/`[^`\n]+`/g) ?? []) {
            acumularMapa(inventario.elementosUi, elementoUi.slice(1, -1));
        }
    }

    inventario.perfis = ordenarMapa(inventario.perfis);
    inventario.situacoes = ordenarMapa(inventario.situacoes);
    inventario.tiposProcesso = ordenarMapa(inventario.tiposProcesso);
    inventario.elementosUi = ordenarMapa(inventario.elementosUi);

    if (emitirJson) {
        imprimirJson(inventario);
        return;
    }

    escreverLinha(`Inventário de vocabulário dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${inventario.totalArquivos}`);
    escreverLinha();
    escreverLinha("Perfis encontrados:");
    for (const [valor, quantidade] of Object.entries(inventario.perfis)) {
        escreverLinha(`- ${quantidade}x ${valor}`);
    }
    escreverLinha();
    escreverLinha("Situações encontradas:");
    for (const [valor, quantidade] of Object.entries(inventario.situacoes)) {
        escreverLinha(`- ${quantidade}x '${valor}'`);
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
