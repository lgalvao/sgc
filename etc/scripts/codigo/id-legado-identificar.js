#!/usr/bin/env node
import path from "node:path";
import fs from "fs-extra";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import logger from "../lib/logger.js";
import {imprimirCabecalho} from "../lib/saida.js";

const EXTENSOES = ["vue", "ts", "js", "html", "java", "kt", "sql", "xml", "yml", "properties"];
const IGNORAR = ["**/node_modules/**", "**/.git/**", "**/.gradle/**", "**/build/**", "**/dist/**", "**/target/**", "**/.idea/**", "**/.vscode/**"];
const SAIDA_PADRAO = "id-legacy-report.txt";
const SEPARADOR = " | ";

const PADROES = [
    /\bid\b/g,
    /\bId\b/g,
    /\{id\}/g,
    /\b\w+Id\b/g,
    /\bid[A-Z]\w*\b/g,
    /\bget[A-Z]\w*Id\b/g,
    /\bset[A-Z]\w*Id\b/g,
    /\bgetId\b/g,
    /\bsetId\b/g,
    /"id"\s*:/g,
    /'id'\s*:/g
];

const EXCECOES_EXATAS = new Set([
    "grid", "invalid", "valid", "solid", "void", "fluid", "width", "mid", "side",
    "rapid", "rigid", "liquid", "acid", "hybrid", "guid", "uuid", "id-br", "id-BR",
    "traceId", "traceid", "TRACEID", "setTraceId", "getTraceId",
    "idAlvo", "UsuarioPerfilId", "UnidadeProcessoId"
]);

const IGNORAR_SPRING = new Set([
    "@Id", "@EmbeddedId", "@MapsId", "@IdClass",
    "findById", "existsById", "deleteById", "findAllById", "countById", "deleteAllById",
    "getReferenceById"
]);

function mostrarAjuda() {
    process.stdout.write(`Uso:
  node etc/scripts/codigo/id-legado-identificar.js [diretorio] [--output <arquivo>]

Exemplos:
  node etc/scripts/sgc.js codigo id-legado identificar
  node etc/scripts/sgc.js codigo id-legado identificar backend/src
`);
}

function deveIgnorarTrecho(texto, linha, indice) {
    const normalizado = texto.trim();
    if (EXCECOES_EXATAS.has(normalizado) || EXCECOES_EXATAS.has(normalizado.toLowerCase())) {
        return true;
    }

    const inicio = Math.max(0, indice - 15);
    const fim = Math.min(linha.length, indice + texto.length + 15);
    const contexto = linha.slice(inicio, fim);
    return [...IGNORAR_SPRING].some((item) => contexto.includes(item));
}

async function listarArquivos(diretorio) {
    const padroes = EXTENSOES.map((extensao) => `${diretorio.replaceAll("\\", "/")}/**/*.${extensao}`);
    return globby(padroes, {
        cwd: resolverNaRaiz(),
        absolute: true,
        onlyFiles: true,
        ignore: IGNORAR
    });
}

async function executarIdentificacao({diretorio = ".", output = SAIDA_PADRAO} = {}) {
    const arquivos = await listarArquivos(diretorio);
    const ocorrencias = [];

    for (const arquivo of arquivos) {
        const conteudo = await fs.readFile(arquivo, "utf-8");
        const linhas = conteudo.split(/\r?\n/);

        linhas.forEach((linha, indice) => {
            const aparicoes = new Set();

            if (linha.trimStart().startsWith("import ") || linha.trimStart().startsWith("package ")) {
                return;
            }

            for (const padrao of PADROES) {
                for (const match of linha.matchAll(padrao)) {
                    const texto = match[0];
                    if (deveIgnorarTrecho(texto, linha, match.index ?? 0)) {
                        continue;
                    }
                    aparicoes.add(texto);
                }
            }

            if (aparicoes.size > 0) {
                ocorrencias.push(`${path.relative(resolverNaRaiz(), arquivo)}${SEPARADOR}${indice + 1}${SEPARADOR}${[...aparicoes].join(", ")}${SEPARADOR}${linha.trim()}`);
            }
        });
    }

    const caminhoSaida = path.resolve(resolverNaRaiz(), output);
    const cabecalho = `ARQUIVO${SEPARADOR}LINHA${SEPARADOR}MATCHES${SEPARADOR}CONTEUDO\n`;
    await fs.outputFile(caminhoSaida, ocorrencias.length > 0 ? `${cabecalho}${ocorrencias.join("\n")}` : `Nenhuma instancia legada de 'id' encontrada em ${diretorio}.`);
    imprimirCabecalho("Identificacao de id legado", `Relatorio salvo em ${caminhoSaida}`);
    process.stdout.write(`Ocorrencias: ${ocorrencias.length}\n`);
}

const args = process.argv.slice(2);
if (args.includes("--help") || args.includes("-h")) {
    mostrarAjuda();
    process.exit(0);
}

const outputIndice = args.indexOf("--output");
const output = outputIndice >= 0 ? args[outputIndice + 1] : SAIDA_PADRAO;
const diretorio = args.find((arg, indice) => !arg.startsWith("-") && indice !== outputIndice + 1) ?? ".";

executarIdentificacao({diretorio, output}).catch((error) => {
    logger.error(`Erro ao identificar id legado: ${error.message}`);
    process.exit(1);
});
