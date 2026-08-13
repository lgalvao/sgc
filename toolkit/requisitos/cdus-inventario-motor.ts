import {
    analisarArquivo,
    extrairCabecalhoFluxo,
    extrairCabecalhoPre,
    extrairLinhaAtor,
    lerArquivo,
    listarArquivosCdu
} from "./cdus-documentos-lib.js";
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

interface InventarioFormatos {
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
        formatos?: InventarioFormatos;
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

function selecionarSecoes(argumentos: readonly string[] | undefined): Set<SecaoInventario> {
    if (!argumentos || argumentos.length === 0 || argumentos.includes("todos")) {
        return new Set(SECOES_INVENTARIO);
    }

    const desconhecidas = argumentos.filter(secao => !SECOES_INVENTARIO.includes(secao as SecaoInventario));
    if (desconhecidas.length > 0) {
        throw new Error(`Seção de inventário CDU desconhecida: ${desconhecidas.join(", ")}.`);
    }

    return new Set(argumentos as SecaoInventario[]);
}

async function inventariarCdus(base: string, secoesInformadas?: readonly string[]): Promise<InventarioCdus> {
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

function acumularMapa(mapa: MapaContagem, chave: string): void {
    mapa[chave] = (mapa[chave] ?? 0) + 1;
}

function ordenarMapa(mapa: MapaContagem): MapaContagem {
    return Object.fromEntries(
        Object.entries(mapa).toSorted((a, b) => b[1] - a[1] || a[0].localeCompare(b[0], "pt-BR"))
    );
}

async function inventariarFormatos(base: string, arquivosInformados?: readonly string[]): Promise<InventarioFormatos> {
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const inventario: InventarioFormatos = {
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

export type {InventarioCdus, InventarioFormatos};
export {inventariarCdus};
