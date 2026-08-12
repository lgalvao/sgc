#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha} from "../lib/saida.js";

function sincronizarVersao(novaVersao, diretorioBase = resolverNaRaiz()) {
    if (!novaVersao) {
        throw new Error("Informe a versão que deve ser sincronizada.");
    }

    const arquivosAtualizados = [];

    const resolver = caminho => path.resolve(diretorioBase, caminho);
    const caminhoGradle = resolver("gradle.properties");
    if (fs.existsSync(caminhoGradle)) {
        let conteudo = fs.readFileSync(caminhoGradle, "utf-8");
        conteudo = conteudo.replace(/^version=.*$/m, `version=${novaVersao}`);
        fs.writeFileSync(caminhoGradle, conteudo, "utf-8");
        arquivosAtualizados.push("gradle.properties");
    }

    const caminhoFrontend = resolver("frontend/package.json");
    if (fs.existsSync(caminhoFrontend)) {
        const pacote = JSON.parse(fs.readFileSync(caminhoFrontend, "utf-8"));
        pacote.version = novaVersao;
        fs.writeFileSync(caminhoFrontend, `${JSON.stringify(pacote, null, 2)}\n`, "utf-8");
        arquivosAtualizados.push("frontend/package.json");
    }

    return {
        novaVersao,
        arquivosAtualizados
    };
}

function principal(argumentos = process.argv.slice(2)) {
    const novaVersao = argumentos[0];
    if (!novaVersao) {
        process.stderr.write("Uso recomendado: node toolkit/sgc.js projeto versao-sincronizar <versao>\n");
        process.stderr.write("Execução direta: node --import=tsx toolkit/projeto/versao-sincronizar.js <versao>\n");
        process.exitCode = 1;
        return;
    }

    const resultado = sincronizarVersao(novaVersao);
    for (const arquivo of resultado.arquivosAtualizados) {
        escreverLinha(`[v] ${arquivo} atualizado para ${novaVersao}`);
    }

    return resultado;
}

if (ehEntradaPrincipal(import.meta.url)) {
    try {
        principal();
    } catch (erro) {
        process.stderr.write(`Erro ao sincronizar versão: ${erro instanceof Error ? erro.message : String(erro)}\n`);
        process.exitCode = 1;
    }
}

export {
    principal,
    sincronizarVersao
};
