#!/usr/bin/env node
import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {escreverLinha, imprimirJson} from "../lib/saida.js";
import {coletarIdentificadores, obterDiretorioBusca} from "./identificadores-teste-lib.js";

const IDENTIFICADORES_COMPARTILHADOS_PERMITIDOS = new Set([
    "subprocesso-header__txt-header-unidade",
]);

function encontrarDuplicados(identificadores) {
    const identificadoresPorValor = Object.groupBy(identificadores, (item) => item.valor);
    return Object.entries(identificadoresPorValor)
        .filter(([valor]) => !IDENTIFICADORES_COMPARTILHADOS_PERMITIDOS.has(valor))
        .filter(([, itens]) => itens.length > 1)
        .toSorted(([, itensA], [, itensB]) => itensB.length - itensA.length)
        .map(([valor, itens]) => ({valor, itens}));
}

function criarResultado(coleta) {
    const duplicados = encontrarDuplicados(coleta.identificadores);
    return {
        diretorioBusca: coleta.diretorioBusca,
        totalIdentificadores: coleta.identificadores.length,
        totalDuplicados: duplicados.length,
        totalOcorrenciasDuplicadas: duplicados.reduce((total, item) => total + item.itens.length, 0),
        duplicados,
    };
}

function imprimirDuplicados(resultado) {
    if (resultado.totalDuplicados === 0) {
        escreverLinha("Nenhum identificador de teste duplicado encontrado.");
        return;
    }

    escreverLinha("Identificadores de teste duplicados encontrados:");
    escreverLinha();
    resultado.duplicados.forEach(({valor, itens}) => {
        escreverLinha(`>> "${valor}" — ${itens.length} ocorrências`);
        itens.forEach((item) => {
            escreverLinha(`   └─ ${item.arquivo}${item.atributo !== "data-testid" ? ` (${item.atributo})` : ""}`);
        });
        escreverLinha();
    });

    escreverLinha(`Total de identificadores duplicados distintos: ${resultado.totalDuplicados}`);
    escreverLinha(`Total de ocorrencias duplicadas: ${resultado.totalOcorrenciasDuplicadas}`);
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend identificadores-teste listar-duplicados",
            scriptDireto: "frontend/identificadores-teste-listar-duplicados.js",
            descricao: "Lista identificadores de teste duplicados nos templates Vue.",
            opcoes: [
                "--json               Emite os duplicados em JSON.",
                "--base <diretorio>   Raiz do projeto para resolver frontendCodigo.",
                "--dir <diretorio>    Sobrescreve o diretório configurado de código.",
            ],
            exemplos: [
                "npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados",
                "npx tsx toolkit/sgc.ts frontend identificadores-teste listar-duplicados --dir /tmp/frontend",
            ],
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const resultado = criarResultado(await coletarIdentificadores(obterDiretorioBusca(argumentos, diretorioBase), diretorioBase));
    if (emitirJson) {
        imprimirJson(resultado);
    } else {
        escreverLinha(`Buscando por identificadores de teste em: ${resultado.diretorioBusca}`);
        escreverLinha();
        imprimirDuplicados(resultado);
    }

    if (resultado.totalDuplicados > 0) {
        process.exitCode = 1;
    }
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao executar a auditoria de identificadores duplicados: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
