#!/usr/bin/env node
import path from "node:path";
import fs from "fs-extra";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import logger from "../lib/logger.js";
import {imprimirCabecalho} from "../lib/saida.js";

const EXTENSOES = ["vue", "ts", "js", "html", "java", "md"];
const IGNORAR = ["**/node_modules/**", "**/.git/**", "**/.gradle/**", "**/build/**", "**/dist/**", "**/target/**", "**/.idea/**", "**/.vscode/**"];
const SAIDA_PADRAO = "title-case-report.txt";
const SEPARADOR = " | ";
const UPPER = "[A-ZÀ-ÖØ-Þ]";
const LOWER = "[a-zà-öø-ÿ]";
const TITULO_CASE = new RegExp(`\\b${UPPER}${LOWER}*(\\s+${UPPER}${LOWER}*)+\\b`, "g");

const EXCECOES_GLOBAIS = [
    "Tribunal Regional Eleitoral", "Justiça Eleitoral", "Spring Boot", "Spring Data", "Spring Security",
    "Node.js", "Vue.js", "JavaScript", "TypeScript", "Playwright", "PostgreSQL", "Oracle", "Docker",
    "Kubernetes", "Windows", "Linux", "Caps Lock", "Bootstrap", "Font Awesome", "List", "Set", "Map"
];

function mostrarAjuda() {
    process.stdout.write(`Uso:
  node etc/scripts/codigo/title-case-identificar.js [diretorio] [--output <arquivo>]

Exemplos:
  node etc/scripts/sgc.js codigo title-case identificar
  node etc/scripts/sgc.js codigo title-case identificar frontend/src
`);
}

function isCamelCase(palavra) {
    return [...palavra.slice(1)].some((letra) => letra === letra.toUpperCase() && letra !== letra.toLowerCase());
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
            const trimmed = linha.trimStart();
            if (trimmed.startsWith("import ") || trimmed.startsWith("@") || trimmed.startsWith("package ")) {
                return;
            }

            for (const match of linha.matchAll(TITULO_CASE)) {
                const texto = match[0].trim();
                const palavras = texto.split(/\s+/);

                if (texto.toUpperCase() === texto) {
                    continue;
                }

                if (EXCECOES_GLOBAIS.some((item) => texto.toLowerCase().includes(item.toLowerCase()))) {
                    continue;
                }

                if (palavras.some(isCamelCase)) {
                    continue;
                }

                if (palavras.length >= 2 && palavras.at(-1).length === 1 && palavras.at(-1).toUpperCase() === palavras.at(-1)) {
                    continue;
                }

                ocorrencias.push(`${path.relative(resolverNaRaiz(), arquivo)}${SEPARADOR}${indice + 1}${SEPARADOR}${texto}`);
            }
        });
    }

    const caminhoSaida = path.resolve(resolverNaRaiz(), output);
    await fs.outputFile(caminhoSaida, ocorrencias.length > 0 ? ocorrencias.join("\n") : "Nenhuma instância encontrada.");
    imprimirCabecalho("Identificacao de Title Case", `Relatorio salvo em ${caminhoSaida}`);
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
    logger.error(`Erro ao identificar Title Case: ${error.message}`);
    process.exit(1);
});
