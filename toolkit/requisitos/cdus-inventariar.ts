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
import {inventariarDensidade} from "./cdus-inventariar-densidade.js";
import {inventariarDuplicacoes} from "./cdus-inventariar-duplicacoes.js";
import {inventariarMensagens} from "./cdus-inventariar-mensagens.js";
import {inventariarVocabulario} from "./cdus-inventariar-vocabulario.js";

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

type SecaoInventario = "formatos" | "vocabulario" | "mensagens" | "densidade" | "duplicacoes";

interface InventarioCdus {
    versao: 1;
    base: string;
    totalArquivos: number;
    secoes: {
        formatos?: InventarioCdu;
        vocabulario?: Awaited<ReturnType<typeof inventariarVocabulario>>;
        mensagens?: Awaited<ReturnType<typeof inventariarMensagens>>;
        densidade?: Awaited<ReturnType<typeof inventariarDensidade>>;
        duplicacoes?: Awaited<ReturnType<typeof inventariarDuplicacoes>>;
    };
}

const SECOES_INVENTARIO: readonly SecaoInventario[] = [
    "formatos",
    "vocabulario",
    "mensagens",
    "densidade",
    "duplicacoes"
];

function selecionarSecoes(argumentos: string[] | undefined): Set<SecaoInventario> {
    if (!argumentos || argumentos.length === 0 || argumentos.includes("todos")) {
        return new Set(SECOES_INVENTARIO);
    }

    const desconhecidas = argumentos.filter(secao => !SECOES_INVENTARIO.includes(secao as SecaoInventario));
    if (desconhecidas.length > 0) {
        throw new Error(`Seção de inventário CDU desconhecida: ${desconhecidas.join(", ")}.`);
    }

    return new Set(argumentos as SecaoInventario[]);
}

async function inventariarCdus(base: string, secoesInformadas?: string[]): Promise<InventarioCdus> {
    const arquivos = await listarArquivosCdu(base);
    const secoesSelecionadas = selecionarSecoes(secoesInformadas);
    const secoes: InventarioCdus["secoes"] = {};

    if (secoesSelecionadas.has("formatos")) {
        secoes.formatos = await inventariarFormatos(base, arquivos);
    }
    if (secoesSelecionadas.has("vocabulario")) {
        secoes.vocabulario = await inventariarVocabulario(base, arquivos);
    }
    if (secoesSelecionadas.has("mensagens")) {
        secoes.mensagens = await inventariarMensagens(base, arquivos);
    }
    if (secoesSelecionadas.has("densidade")) {
        secoes.densidade = await inventariarDensidade(base, arquivos);
    }
    if (secoesSelecionadas.has("duplicacoes")) {
        secoes.duplicacoes = await inventariarDuplicacoes(base, arquivos);
    }

    return {versao: 1, base, totalArquivos: arquivos.length, secoes};
}

function imprimirResumoMapa(titulo: string, mapa: MapaContagem): void {
    const itens = Object.entries(mapa).slice(0, 8);
    escreverLinha(`${titulo}: ${Object.keys(mapa).length} valores`);
    for (const [valor, quantidade] of itens) {
        escreverLinha(`  ${quantidade}x ${valor}`);
    }
}

function imprimirInventarioCdus(resultado: InventarioCdus): void {
    escreverLinha(`Inventário consolidado dos CDUs em ${path.join(resultado.base, "specs")}`);
    escreverLinha(`Arquivos analisados: ${resultado.totalArquivos}`);
    escreverLinha();

    const {formatos, vocabulario, mensagens, densidade, duplicacoes} = resultado.secoes;
    if (formatos) {
        escreverLinha("[formatos]");
        imprimirResumoMapa("Atores", formatos.formatosAtor);
        imprimirResumoMapa("Pré-condições", formatos.formatosPreCondicoes);
        imprimirResumoMapa("Fluxo principal", formatos.formatosFluxoPrincipal);
        escreverLinha(`Numeração repetida: ${formatos.documentosComReinicioNumeracao.length}`);
        escreverLinha(`Numeração regressiva: ${formatos.documentosComRegressaoNumeracao.length}`);
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
    }
}

function acumularMapa(mapa: MapaContagem, chave: string): void {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa: MapaContagem): MapaContagem {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

async function inventariarFormatos(base: string, arquivosInformados?: string[]): Promise<InventarioCdu> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
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

    return inventario;
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const {emitirJson, base, secoes} = obterOpcoesCdu(argumentos);
    const resultado = await inventariarCdus(base, secoes);

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirInventarioCdus(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    await principal();
}

export {
    inventariarCdus,
    inventariarFormatos,
    principal
};
