import fs from "node:fs";
import path from "node:path";
import {globby} from "globby";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {carregarConfiguracao} from "../lib/configuracao.js";

const REGEX_TITULO = /^#\s+CDU-(\d{2})\s+-\s+(.+)$/m;
const REGEX_SECAO_ATORES = /^##\s+Atores\s*$/m;
const REGEX_SECAO_PRE = /^##\s+Pré-condições\s*$/m;
const REGEX_SECAO_FLUXO = /^##\s+Fluxo principal\s*$/m;
const REGEX_PASSO = /^(\d+)\.\s+/gm;
const REGEX_LINK_CDU = /\[[^\]]+\]\(([^)]+)\)/g;
const REGEX_PLACEHOLDER_LEGADO = /\[[A-Z0-9_]+\]/g;
const REGEX_PLACEHOLDER_CANONICO = /:[A-Z0-9_]+:/g;
const REGEX_UI_CRONICA = /`[^`]+`/g;
const REGEX_SITUACOES = /'[^'\n]+'/g;

interface OpcoesCdu {
    emitirJson: boolean;
    base: string;
    secoes?: string[];
}

interface IndicesSecoes {
    ator: number;
    pre: number;
    fluxo: number;
}

interface ContagensCdu {
    placeholdersCanonicos: number;
    placeholdersLegados: number;
    uiEmCrases: number;
    situacoesEntreAspas: number;
    palavras: number;
}

interface AnaliseCdu {
    caminhoArquivo: string;
    nomeArquivo: string;
    texto: string;
    linhas: string[];
    tituloNumero: string | null;
    tituloTexto: string | null;
    temTituloCanonico: boolean;
    temAtores: boolean;
    quantidadeSecoesAtoresCanonicas: number;
    temPre: boolean;
    temFluxo: boolean;
    indices: IndicesSecoes;
    passos: number[];
    repeticoes: number[];
    regressoes: string[];
    quantidadeAtores: number;
    quantidadePreCondicoes: number;
    linksMarkdown: string[];
    contagens: ContagensCdu;
}

function normalizarCaminho(caminho: string): string {
    return caminho.replaceAll("\\", "/");
}

function obterOpcoesCdu(argumentos: string[] = process.argv.slice(2)): OpcoesCdu {
    let emitirJson = false;
    let baseInformada: string | undefined;
    let secoes: string[] | undefined;

    for (let indice = 0; indice < argumentos.length; indice += 1) {
        const argumento = argumentos[indice];
        if (argumento === "--json") {
            emitirJson = true;
            continue;
        }

        if (argumento === "--base" || argumento === "--secoes") {
            const valor = argumentos[indice + 1];
            if (!valor || valor.startsWith("--")) {
                throw new Error(`A opção ${argumento} exige um valor.`);
            }
            indice += 1;
            if (argumento === "--base") {
                baseInformada = valor;
            } else {
                secoes = valor.split(",").map(secao => secao.trim()).filter(Boolean);
            }
            continue;
        }

        throw new Error(`Opção ou argumento CDU desconhecido: ${argumento}`);
    }

    const base = baseInformada ? path.resolve(baseInformada) : DIRETORIO_RAIZ;
    return {emitirJson, base, secoes};
}

async function listarArquivosCdu(base: string = DIRETORIO_RAIZ): Promise<string[]> {
    const padraoConfigurado = carregarConfiguracao(base).requisitos.cdus.padraoArquivos;
    const padrao = normalizarCaminho(path.resolve(base, padraoConfigurado));
    const arquivos = await globby(padrao, {absolute: true});
    return arquivos.toSorted((a, b) => a.localeCompare(b, "pt-BR", {numeric: true}));
}

function lerArquivo(caminho: string): string {
    return fs.readFileSync(caminho, "utf8");
}

function obterLinhas(texto: string): string[] {
    return texto.split(/\r?\n/);
}

function encontrarIndicesSecoes(linhas: string[]): IndicesSecoes {
    return {
        ator: linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha)),
        pre: linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha)),
        fluxo: linhas.findIndex(linha => /^##\s+Fluxo principal\s*$/.test(linha))
    };
}

function contarOcorrencias(linhas: string[], regex: RegExp): number {
    return linhas.filter(linha => regex.test(linha)).length;
}

function extrairPassosNumerados(texto: string): number[] {
    return [...texto.matchAll(REGEX_PASSO)].map(correspondencia => Number(correspondencia[1]));
}

function localizarLinksInternosCdu(texto: string): string[] {
    return [...texto.matchAll(REGEX_LINK_CDU)]
        .map(correspondencia => correspondencia[1])
        .filter(destino => destino.endsWith(".md"));
}

function resolverDestinoMarkdown(caminhoArquivo: string, destino: string): string | null {
    // noinspection HttpUrlsUsage
    if (destino.startsWith("http://") || destino.startsWith("https://") || destino.startsWith("#")) {
        return null;
    }

    return path.resolve(path.dirname(caminhoArquivo), destino);
}

function analisarArquivo(caminhoArquivo: string, texto: string): AnaliseCdu {
    const nomeArquivo = path.basename(caminhoArquivo);
    const linhas = obterLinhas(texto);
    const titulo = texto.match(REGEX_TITULO);
    const temAtores = REGEX_SECAO_ATORES.test(texto);
    const temPre = REGEX_SECAO_PRE.test(texto);
    const temFluxo = REGEX_SECAO_FLUXO.test(texto);
    const indices = encontrarIndicesSecoes(linhas);
    const passos = extrairPassosNumerados(texto);
    const atores = linhas
        .slice(indices.ator + 1, indices.pre > indices.ator ? indices.pre : undefined)
        .filter(linha => /^\s*-\s+/.test(linha));
    const preCondicoes = linhas
        .slice(indices.pre + 1, indices.fluxo > indices.pre ? indices.fluxo : undefined)
        .filter(linha => /^\s*-\s+/.test(linha));

    const repeticoes: number[] = [];
    const regressoes: string[] = [];
    for (let i = 1; i < passos.length; i += 1) {
        if (passos[i] === passos[i - 1]) {
            repeticoes.push(passos[i]);
        }

        if (passos[i] < passos[i - 1]) {
            regressoes.push(`${passos[i - 1]}->${passos[i]}`);
        }
    }

    return {
        caminhoArquivo,
        nomeArquivo,
        texto,
        linhas,
        tituloNumero: titulo?.[1] ?? null,
        tituloTexto: titulo?.[2] ?? null,
        temTituloCanonico: Boolean(titulo),
        temAtores,
        quantidadeSecoesAtoresCanonicas: contarOcorrencias(linhas, /^##\s+Atores\s*$/),
        temPre,
        temFluxo,
        indices,
        passos,
        repeticoes: [...new Set(repeticoes)],
        regressoes: [...new Set(regressoes)],
        quantidadeAtores: atores.length,
        quantidadePreCondicoes: preCondicoes.length,
        linksMarkdown: localizarLinksInternosCdu(texto),
        contagens: {
            placeholdersCanonicos: (texto.match(REGEX_PLACEHOLDER_CANONICO) ?? []).length,
            placeholdersLegados: (texto.match(REGEX_PLACEHOLDER_LEGADO) ?? []).length,
            uiEmCrases: (texto.match(REGEX_UI_CRONICA) ?? []).length,
            situacoesEntreAspas: (texto.match(REGEX_SITUACOES) ?? []).length,
            palavras: texto.split(/\s+/).filter(Boolean).length
        }
    };
}

function validarLinksMarkdown(analise: AnaliseCdu): string[] {
    const invalidos: string[] = [];

    for (const destino of analise.linksMarkdown) {
        const resolvido = resolverDestinoMarkdown(analise.caminhoArquivo, destino);
        if (resolvido && !fs.existsSync(resolvido)) {
            invalidos.push(destino);
        }
    }

    return invalidos;
}

function extrairLinhaAtor(texto: string): string | null {
    const linhas = obterLinhas(texto);
    return linhas.find(linha => /Atores|Ator(?:es)?/.test(linha)) ?? null;
}

function extrairCabecalhoPre(texto: string): string | null {
    const linhas = obterLinhas(texto);
    return linhas.find(linha => /Pré-condiç(?:ão|ões)/.test(linha)) ?? null;
}

function extrairCabecalhoFluxo(texto: string): string | null {
    const linhas = obterLinhas(texto);
    return linhas.find(linha => /Fluxo principal/.test(linha)) ?? null;
}

export {
    analisarArquivo,
    extrairCabecalhoFluxo,
    extrairCabecalhoPre,
    extrairLinhaAtor,
    listarArquivosCdu,
    lerArquivo,
    obterOpcoesCdu,
    obterLinhas,
    validarLinksMarkdown
};
