// Comando de inventário consolidado dos casos de uso CDU.

import path from "node:path";
import {ehEntradaPrincipal, validarArgumentosEntradaDireta} from "../biblioteca/execucao.js";
import {escreverLinha, imprimirJson} from "../biblioteca/saida.js";
import {obterOpcoesCdu} from "./cdus-opcoes.js";
import {inventariarCdus, type InventarioCdus, type InventarioFormatos} from "./cdus-inventario-motor.js";

const LIMITE_ITENS_RESUMO = 20;

function limitarMapa(mapa: Record<string, number>): Record<string, number> {
    return Object.fromEntries(Object.entries(mapa).slice(0, LIMITE_ITENS_RESUMO));
}

function limitarTexto(texto: string, limite = 240): string {
    return texto.length <= limite ? texto : `${texto.slice(0, limite - 1)}…`;
}

function criarResumoJson(resultado: InventarioCdus): unknown {
    const formatos = resultado.secoes.formatos;
    const vocabulario = resultado.secoes.vocabulario;
    const mensagens = resultado.secoes.mensagens;
    const densidade = resultado.secoes.densidade;
    const duplicacoes = resultado.secoes.duplicacoes;
    const secoes: Record<string, unknown> = {};

    if (formatos) {
        secoes.formatos = {
            totalArquivos: formatos.totalArquivos,
            formatosAtor: limitarMapa(formatos.formatosAtor),
            formatosPreCondicoes: limitarMapa(formatos.formatosPreCondicoes),
            formatosFluxoPrincipal: limitarMapa(formatos.formatosFluxoPrincipal),
            documentosComReinicioNumeracao: formatos.documentosComReinicioNumeracao.slice(0, LIMITE_ITENS_RESUMO),
            documentosComRegressaoNumeracao: formatos.documentosComRegressaoNumeracao.slice(0, LIMITE_ITENS_RESUMO),
            situacoesMaisFrequentes: limitarMapa(formatos.situacoesMaisFrequentes),
            elementosUiMaisFrequentes: limitarMapa(formatos.elementosUiMaisFrequentes),
            placeholdersMaisFrequentes: limitarMapa(formatos.placeholdersMaisFrequentes)
        };
    }
    if (vocabulario) {
        secoes.vocabulario = {
            totalArquivos: vocabulario.totalArquivos,
            perfis: limitarMapa(vocabulario.perfis),
            situacoes: limitarMapa(vocabulario.situacoes),
            tiposProcesso: limitarMapa(vocabulario.tiposProcesso),
            elementosUi: limitarMapa(vocabulario.elementosUi),
            canonicos: {
                perfis: vocabulario.canonicos.perfis.slice(0, LIMITE_ITENS_RESUMO),
                situacoes: vocabulario.canonicos.situacoes.slice(0, LIMITE_ITENS_RESUMO),
                tiposProcesso: vocabulario.canonicos.tiposProcesso.slice(0, LIMITE_ITENS_RESUMO)
            }
        };
    }
    if (mensagens) {
        secoes.mensagens = {
            totalArquivos: mensagens.totalArquivos,
            descricoes: limitarMapa(mensagens.descricoes),
            assuntos: limitarMapa(mensagens.assuntos),
            mensagens: limitarMapa(mensagens.mensagens),
            toasts: limitarMapa(mensagens.toasts)
        };
    }
    if (densidade) {
        secoes.densidade = {
            totalArquivos: densidade.totalArquivos,
            resumo: densidade.resumo,
            documentos: densidade.documentos.slice(0, LIMITE_ITENS_RESUMO)
        };
    }
    if (duplicacoes) {
        secoes.duplicacoes = {
            totalArquivos: duplicacoes.totalArquivos,
            totalDuplicacoes: duplicacoes.duplicacoes.length,
            duplicacoes: duplicacoes.duplicacoes.slice(0, LIMITE_ITENS_RESUMO).map(duplicacao => ({
                ...duplicacao,
                arquivos: duplicacao.arquivos.slice(0, 5),
                amostra: limitarTexto(duplicacao.amostra)
            }))
        };
    }

    return {
        versaoResumo: 1,
        versao: resultado.versao,
        base: resultado.base,
        totalArquivos: resultado.totalArquivos,
        truncado: true,
        limiteItens: LIMITE_ITENS_RESUMO,
        secoes
    };
}

function imprimirResumoMapa(titulo: string, mapa: Record<string, number>): void {
    const itens = Object.entries(mapa)
        .toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
        .slice(0, 8);
    escreverLinha(`${titulo}: ${Object.keys(mapa).length} valores`);
    for (const [valor, quantidade] of itens) {
        escreverLinha(`  ${quantidade}x ${valor}`);
    }
}

function imprimirFormatos(formatos: InventarioFormatos): void {
    imprimirResumoMapa("Atores", formatos.formatosAtor);
    imprimirResumoMapa("Pré-condições", formatos.formatosPreCondicoes);
    imprimirResumoMapa("Fluxo principal", formatos.formatosFluxoPrincipal);
    escreverLinha(`Numeração repetida: ${formatos.documentosComReinicioNumeracao.length}`);
    escreverLinha(`Numeração regressiva: ${formatos.documentosComRegressaoNumeracao.length}`);
}

function imprimirInventario(resultado: InventarioCdus): void {
    escreverLinha(`Inventário consolidado dos CDUs em ${path.join(resultado.base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha();

    const {formatos, vocabulario, mensagens, densidade, duplicacoes} = resultado.secoes;
    if (formatos) {
        escreverLinha("[formatos]");
        imprimirFormatos(formatos);
        escreverLinha();
    }
    if (vocabulario) {
        escreverLinha("[vocabulário]");
        imprimirResumoMapa("Perfis", vocabulario.perfis);
        imprimirResumoMapa("Situações", vocabulario.situacoes);
        imprimirResumoMapa("Tipos de processo", vocabulario.tiposProcesso);
        imprimirResumoMapa("Elementos de interface", vocabulario.elementosUi);
        escreverLinha();
    }
    if (mensagens) {
        escreverLinha("[mensagens]");
        imprimirResumoMapa("Descrições", mensagens.descricoes);
        imprimirResumoMapa("Assuntos", mensagens.assuntos);
        imprimirResumoMapa("Mensagens", mensagens.mensagens);
        imprimirResumoMapa("Toasts", mensagens.toasts);
        escreverLinha();
    }
    if (densidade) {
        escreverLinha("[densidade]");
        escreverLinha(`Média: ${densidade.resumo.mediaPalavras.toFixed(1)} palavras e ${densidade.resumo.mediaPassos.toFixed(1)} passos`);
        escreverLinha(`Máximo: ${densidade.resumo.maxPalavras} palavras e ${densidade.resumo.maxPassos} passos`);
        escreverLinha();
    }
    if (duplicacoes) {
        escreverLinha("[duplicações]");
        escreverLinha(`Itens duplicados: ${duplicacoes.duplicacoes.length}`);
        duplicacoes.duplicacoes.slice(0, 5).forEach((duplicacao, indice) => {
            escreverLinha(`${indice + 1}. [${duplicacao.tipo}] ${duplicacao.ocorrencias} ocorrência(s) em ${duplicacao.arquivos.slice(0, 3).join(", ")}`);
            escreverLinha(`   Amostra: ${duplicacao.amostra.split("\n")[0]}`);
        });
    }
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const argumentosValidados = validarArgumentosEntradaDireta(import.meta.url, argumentos);
    const {emitirJson, emitirJsonResumido, base, secoes} = obterOpcoesCdu(argumentosValidados);
    const resultado = await inventariarCdus(base, secoes);

    if (emitirJsonResumido) {
        imprimirJson(criarResumoJson(resultado));
        return;
    }

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirInventario(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}
