#!/usr/bin/env node
import http from "node:http";
import process from "node:process";
import handler from "serve-handler";
import {resolverNaRaiz} from "../lib/caminhos.js";
import logger from "../lib/logger.js";
import {imprimirCabecalho} from "../lib/saida.js";

function obterArgumento(nome, padrao) {
    const indice = process.argv.indexOf(nome);
    if (indice >= 0 && process.argv[indice + 1]) {
        return process.argv[indice + 1];
    }
    return padrao;
}

if (process.argv.includes("--help") || process.argv.includes("-h")) {
    process.stdout.write(`Uso:
  node etc/scripts/qa/dashboard-servir.js [--host 127.0.0.1] [--porta 4179]

Exemplos:
  node etc/scripts/sgc.js qa dashboard servir
  node etc/scripts/sgc.js qa dashboard servir --porta 4180
`);
    process.exit(0);
}

const host = obterArgumento("--host", "127.0.0.1");
const porta = Number.parseInt(obterArgumento("--porta", "4179"), 10);
const diretorioRaiz = resolverNaRaiz();
const caminhoDashboard = "/etc/qa-dashboard/dashboard.html";

const servidor = http.createServer((request, response) => handler(request, response, {
    public: diretorioRaiz,
    cleanUrls: false
}));

servidor.listen(porta, host, () => {
    imprimirCabecalho("QA Dashboard", "Servidor iniciado.");
    // noinspection HttpUrlsUsage
    process.stdout.write(`Abra: http://${host}:${porta}${caminhoDashboard}\n`);
});

servidor.on("error", (error) => {
    logger.error(`Erro ao iniciar servidor do dashboard: ${error.message}`);
    process.exit(1);
});
