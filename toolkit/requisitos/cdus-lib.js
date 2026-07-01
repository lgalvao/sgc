import fs from "node:fs";
import path from "node:path";
import {globby} from "globby";

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

function normalizarCaminho(caminho) {
    return caminho.replaceAll("\\", "/");
}

async function listarArquivosCdu(base = process.cwd()) {
    const padrao = normalizarCaminho(path.join(base, "specs", "cdu-*.md"));
    const arquivos = await globby(padrao, {absolute: true});
    return arquivos.sort((a, b) => a.localeCompare(b, "pt-BR", {numeric: true}));
}

function lerArquivo(caminho) {
    return fs.readFileSync(caminho, "utf8");
}

function obterLinhas(texto) {
    return texto.split(/\r?\n/);
}

function encontrarIndicesSecoes(linhas) {
    return {
        ator: linhas.findIndex(linha => /^##\s+Atores\s*$/.test(linha)),
        pre: linhas.findIndex(linha => /^##\s+Pré-condições\s*$/.test(linha)),
        fluxo: linhas.findIndex(linha => /^##\s+Fluxo principal\s*$/.test(linha))
    };
}

function contarOcorrencias(linhas, regex) {
    return linhas.filter(linha => regex.test(linha)).length;
}

function extrairPassosNumerados(texto) {
    return [...texto.matchAll(REGEX_PASSO)].map(correspondencia => Number(correspondencia[1]));
}

function localizarLinksInternosCdu(texto) {
    return [...texto.matchAll(REGEX_LINK_CDU)]
        .map(correspondencia => correspondencia[1])
        .filter(destino => destino.endsWith(".md"));
}

function resolverDestinoMarkdown(caminhoArquivo, destino) {
    // noinspection HttpUrlsUsage
    if (destino.startsWith("http://") || destino.startsWith("https://") || destino.startsWith("#")) {
        return null;
    }

    return path.resolve(path.dirname(caminhoArquivo), destino);
}

function analisarArquivo(caminhoArquivo, texto) {
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

    const repeticoes = [];
    const regressoes = [];
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

function validarLinksMarkdown(analise) {
    const invalidos = [];

    for (const destino of analise.linksMarkdown) {
        const resolvido = resolverDestinoMarkdown(analise.caminhoArquivo, destino);
        if (resolvido && !fs.existsSync(resolvido)) {
            invalidos.push(destino);
        }
    }

    return invalidos;
}

function extrairLinhaAtor(texto) {
    const linhas = obterLinhas(texto);
    return linhas.find(linha => /Atores|Ator(?:es)?/.test(linha)) ?? null;
}

function extrairCabecalhoPre(texto) {
    const linhas = obterLinhas(texto);
    return linhas.find(linha => /Pré-condiç(?:ão|ões)/.test(linha)) ?? null;
}

function extrairCabecalhoFluxo(texto) {
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
    obterLinhas,
    validarLinksMarkdown
};
