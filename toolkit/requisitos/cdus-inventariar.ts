#!/usr/bin/env node
// Inventário dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {
    analisarArquivo,
    extrairCabecalhoFluxo,
    extrairCabecalhoPre,
    extrairLinhaAtor,
    lerArquivo,
    listarArquivosCdu,
    obterOpcoesCdu
} from "./cdus-lib.js";

type MapaContagem = Record<string, number>;

interface DocumentoNumeracao {
    arquivo: string;
    repeticoes?: number[];
    regressoes?: string[];
}

interface InventarioCdu {
    base: string;
    totalArquivos: number;
    formatosAtor: MapaContagem;
    formatosPreCondicoes: MapaContagem;
    formatosFluxoPrincipal: MapaContagem;
    documentosComReinicioNumeracao: DocumentoNumeracao[];
    documentosComRegressaoNumeracao: DocumentoNumeracao[];
    situacoesMaisFrequentes: MapaContagem;
    elementosUiMaisFrequentes: MapaContagem;
    placeholdersMaisFrequentes: MapaContagem;
}

function acumularMapa(mapa: MapaContagem, chave: string): void {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa: MapaContagem): MapaContagem {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base} = obterOpcoesCdu(argumentos);

    const arquivos = await listarArquivosCdu(base);
    const inventario: InventarioCdu = {
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
        return;
    }

    escreverLinha(`Inventário dos CDUs em ${path.join(base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${inventario.totalArquivos}`);
    escreverLinha();
    escreverLinha("Formatos de ator:");
    for (const [formato, quantidade] of Object.entries(inventario.formatosAtor)) {
        escreverLinha(`- ${quantidade}x ${formato}`);
    }
    escreverLinha();
    escreverLinha("Formatos de pré-condições:");
    for (const [formato, quantidade] of Object.entries(inventario.formatosPreCondicoes)) {
        escreverLinha(`- ${quantidade}x ${formato}`);
    }
    escreverLinha();
    escreverLinha("Formatos de fluxo principal:");
    for (const [formato, quantidade] of Object.entries(inventario.formatosFluxoPrincipal)) {
        escreverLinha(`- ${quantidade}x ${formato}`);
    }
    escreverLinha();
    escreverLinha(`Documentos com repetição de numeração: ${inventario.documentosComReinicioNumeracao.length}`);
    escreverLinha(`Documentos com regressão de numeração: ${inventario.documentosComRegressaoNumeracao.length}`);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    principal
};
