#!/usr/bin/env node
import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {coletarIdentificadores, obterDiretorioBusca} from "./identificadores-teste-lib.js";

function imprimirIdentificadores(resultado) {
    if (resultado.identificadores.length === 0) {
        escreverLinha("Nenhum identificador de teste encontrado.");
        return;
    }

    const identificadoresPorArquivo = Object.groupBy(resultado.identificadores, (item) => item.arquivo);
    for (const [arquivo, identificadores] of Object.entries(identificadoresPorArquivo)) {
        escreverLinha(`Arquivo: ${arquivo}`);
        identificadores.forEach((item) => escreverLinha(`   └─ ${item.atributo}="${item.valor}"`));
        escreverLinha();
    }

    escreverLinha(`Total encontrado: ${resultado.identificadores.length}`);
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend identificadores-teste listar",
            scriptDireto: "frontend/identificadores-teste-listar.js",
            descricao: "Lista identificadores de teste declarados em templates Vue.",
            opcoes: [
                "--json               Emite os identificadores em JSON.",
                "--base <diretorio>   Raiz do projeto para resolver frontendCodigo.",
                "--dir <diretorio>    Sobrescreve o diretório configurado de código.",
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend identificadores-teste listar",
                "npx tsx toolkit/sgc.ts frontend identificadores-teste listar --dir /tmp/frontend",
            ],
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const resultado = await coletarIdentificadores(obterDiretorioBusca(argumentos, diretorioBase), diretorioBase);
    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    escreverLinha(`Buscando por identificadores de teste em: ${resultado.diretorioBusca}`);
    escreverLinha();
    imprimirIdentificadores(resultado);
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao executar a listagem de identificadores: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
