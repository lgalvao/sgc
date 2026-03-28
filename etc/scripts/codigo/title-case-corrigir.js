#!/usr/bin/env node
import path from "node:path";
import fs from "fs-extra";
import {resolverNaRaiz} from "../lib/caminhos.js";
import logger from "../lib/logger.js";
import {imprimirCabecalho} from "../lib/saida.js";

const ENTRADA_PADRAO = "title-case-report.txt";
const SEPARADOR = " | ";

function mostrarAjuda() {
    process.stdout.write(`Uso:
  node etc/scripts/codigo/title-case-corrigir.js [--input <arquivo>] [--dry-run]

Exemplos:
  node etc/scripts/sgc.js codigo title-case corrigir
  node etc/scripts/sgc.js codigo title-case corrigir --dry-run
`);
}

function paraSentenceCase(texto) {
    const palavras = texto.split(/\s+/);
    if (palavras.length === 0) {
        return texto;
    }

    const [primeira, ...restante] = palavras;
    const processadas = restante.map((palavra) => {
        if (palavra.length === 1 && palavra.toUpperCase() === palavra) {
            return palavra;
        }
        return palavra.toLowerCase();
    });

    return [primeira, ...processadas].join(" ");
}

async function executarCorrecao({input = ENTRADA_PADRAO, dryRun = false} = {}) {
    const caminhoEntrada = path.resolve(resolverNaRaiz(), input);
    if (!(await fs.pathExists(caminhoEntrada))) {
        throw new Error(`Relatorio nao encontrado: ${caminhoEntrada}`);
    }

    const conteudo = await fs.readFile(caminhoEntrada, "utf-8");
    const linhas = conteudo.split(/\r?\n/).filter(Boolean);
    const ajustesPorArquivo = new Map();

    for (const linha of linhas) {
        const partes = linha.split(SEPARADOR);
        if (partes.length < 3) {
            continue;
        }

        const [arquivo, numeroLinha, ...resto] = partes;
        const antigo = resto.join(SEPARADOR).trim();
        const lista = ajustesPorArquivo.get(arquivo.trim()) ?? [];
        lista.push({numeroLinha: Number.parseInt(numeroLinha.trim(), 10), antigo});
        ajustesPorArquivo.set(arquivo.trim(), lista);
    }

    let arquivosAtualizados = 0;
    for (const [arquivoRelativo, ajustes] of ajustesPorArquivo.entries()) {
        const caminhoArquivo = path.resolve(resolverNaRaiz(), arquivoRelativo);
        if (!(await fs.pathExists(caminhoArquivo))) {
            continue;
        }

        const linhasArquivo = (await fs.readFile(caminhoArquivo, "utf-8")).split(/\r?\n/);
        let alterado = false;

        for (const ajuste of ajustes.sort((a, b) => b.numeroLinha - a.numeroLinha)) {
            const indice = ajuste.numeroLinha - 1;
            const linhaAtual = linhasArquivo[indice];
            if (!linhaAtual || !linhaAtual.includes(ajuste.antigo)) {
                continue;
            }

            const novo = paraSentenceCase(ajuste.antigo);
            if (novo === ajuste.antigo) {
                continue;
            }

            linhasArquivo[indice] = linhaAtual.replace(ajuste.antigo, novo);
            alterado = true;
        }

        if (alterado) {
            arquivosAtualizados += 1;
            if (!dryRun) {
                await fs.writeFile(caminhoArquivo, linhasArquivo.join("\n"), "utf-8");
            }
        }
    }

    imprimirCabecalho("Correcao de Title Case", dryRun ? "Modo simulacao." : "Ajustes aplicados.");
    process.stdout.write(`Arquivos atualizados: ${arquivosAtualizados}\n`);
}

const args = process.argv.slice(2);
if (args.includes("--help") || args.includes("-h")) {
    mostrarAjuda();
    process.exit(0);
}

const inputIndice = args.indexOf("--input");
const input = inputIndice >= 0 ? args[inputIndice + 1] : ENTRADA_PADRAO;
const dryRun = args.includes("--dry-run");

executarCorrecao({input, dryRun}).catch((error) => {
    logger.error(`Erro ao corrigir Title Case: ${error.message}`);
    process.exit(1);
});
