#!/usr/bin/env node
import fs from "fs-extra";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import logger from "../lib/logger.js";
import {imprimirCabecalho} from "../lib/saida.js";

const IGNORAR = ["**/node_modules/**", "**/.git/**", "**/build/**", "**/dist/**"];
const EXTENSOES = ["ts", "js", "vue", "java"];

const FRASES = [
    "This function", "This class", "This method", "Here we",
    "We need to", "The purpose of", "As requested", "This component",
    "Essa função", "Essa classe", "Esse método", "Aqui nós", "Nós precisamos",
    "O objetivo desse", "Como solicitado", "Esse componente"
];

const INICIOS_OBVIOS = [
    "cria um", "retorna um", "retorna a", "retorna o", "atualiza o", "atualiza a",
    "busca um", "busca o", "busca a", "deleta o", "deleta a", "salva o", "salva a",
    "verifica se", "checa se", "valida o", "valida a", "valida se",
    "função para", "método para", "classe que", "componente que",
    "import", "imports", "define", "definição de", "declaração de",
    "inicializa", "inicia o", "inicia a", "configura o", "configura a",
    "renderiza o", "renderiza a", "estado de", "variável que",
    "adiciona o", "adiciona a", "remove o", "remove a",
    "lida com", "trata o", "trata a", "trata erro",
    "exporta", "export default", "export", "interface", "tipo",
    "chama o", "chama a", "executa o", "executa a",
    "define a props", "define as props", "define as propriedades", "propriedades",
    "importa", "importa os", "importa as"
];

function mostrarAjuda() {
    process.stdout.write(`Uso:
  node etc/scripts/codigo/comentarios-limpar-generico.js [--dry-run]

Exemplos:
  node etc/scripts/sgc.js codigo comentarios limpar-generico
  node etc/scripts/sgc.js codigo comentarios limpar-generico --dry-run
`);
}

function comentarioDescartavel(texto) {
    const lower = texto.toLowerCase();
    if (lower.startsWith("jacoco") || lower.startsWith("eslint") || lower.startsWith("@ts") || lower.startsWith("noinspection") || lower === "ignore") {
        return false;
    }

    if (texto.split(/\s+/).length <= 2) {
        return false;
    }

    if (FRASES.some((frase) => lower.startsWith(frase.toLowerCase()))) {
        return true;
    }

    return INICIOS_OBVIOS.some((frase) => lower.startsWith(frase));
}

async function executarLimpeza({dryRun = false} = {}) {
    const padroes = EXTENSOES.map((extensao) => `**/*.${extensao}`);
    const arquivos = await globby(padroes, {
        cwd: resolverNaRaiz(),
        absolute: true,
        ignore: IGNORAR,
        onlyFiles: true
    });

    let modificados = 0;
    for (const arquivo of arquivos) {
        const original = await fs.readFile(arquivo, "utf-8");
        const linhas = original.split(/\r?\n/);
        const novas = linhas.filter((linha) => {
            const trimmed = linha.trim();
            if (!trimmed.startsWith("//")) {
                return true;
            }

            return !comentarioDescartavel(trimmed.slice(2).trim());
        });

        const atualizado = novas.join("\n");
        if (atualizado !== original) {
            modificados += 1;
            if (!dryRun) {
                await fs.writeFile(arquivo, atualizado, "utf-8");
            }
        }
    }

    imprimirCabecalho("Limpeza generica de comentarios", dryRun ? "Modo simulacao." : "Ajustes aplicados.");
    process.stdout.write(`Arquivos alterados: ${modificados}\n`);
}

const args = process.argv.slice(2);
if (args.includes("--help") || args.includes("-h")) {
    mostrarAjuda();
    process.exit(0);
}

executarLimpeza({dryRun: args.includes("--dry-run")}).catch((error) => {
    logger.error(`Erro ao limpar comentarios: ${error.message}`);
    process.exit(1);
});
