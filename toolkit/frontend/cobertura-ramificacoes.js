#!/usr/bin/env node
import pc from "picocolors";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

function calcularBranchesPerdidos(arquivo) {
    return Math.max(0, arquivo.branchesTotal - Math.round((arquivo.branchesPercentual / 100) * arquivo.branchesTotal));
}

async function principal(argumentos = process.argv.slice(2)) {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "frontend cobertura ramificacoes",
            scriptDireto: "frontend/cobertura-ramificacoes.js",
            descricao: "Lista lacunas de cobertura de ramificacoes no frontend por arquivo.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite <n>    Limita a quantidade de arquivos exibidos. Padrão: 20.",
                "--arquivo <arquivo> Usa um relatório V8 específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", DIRETORIO_RAIZ));
    const caminhoRelatorio = lerOpcao(argumentos, "--arquivo", undefined);
    const limite = Number.parseInt(lerOpcao(argumentos, "--limite", "20"), 10);
    const coleta = await extrairCoberturaFrontend(caminhoRelatorio, {diretorioBase});
    const arquivos = coleta.arquivos
        .map((arquivo) => ({
            arquivo: arquivo.arquivo,
            branchesTotal: arquivo.branchesTotal,
            branchesPercentual: arquivo.branchesPercentual,
            branchesPerdidos: calcularBranchesPerdidos(arquivo)
        }))
        .filter((arquivo) => arquivo.branchesPerdidos > 0)
        .toSorted((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite);

    const resultado = {
        status: "ok",
        timestamp: new Date().toISOString(),
        totais: coleta.branches,
        arquivos
    };

    if (emitirJson) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("COBERTURA DE BRANCHES FRONTEND");
    escreverLinha(`Cobertura global de branches: ${pc.bold(`${coleta.branches.percentual}%`)} (${coleta.branches.cobertos}/${coleta.branches.total})`);
    escreverLinha("");

    if (arquivos.length === 0) {
        escreverLinha(pc.green("Nenhuma lacuna de branches encontrada nos arquivos auditados."));
        return;
    }

    escreverLinha(pc.bold(pc.underline(`TOP ${arquivos.length} ARQUIVOS COM LACUNAS DE BRANCHES:`)));
    arquivos.forEach((arquivo, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(arquivo.arquivo)}`);
        escreverLinha(`   Branches perdidos: ${arquivo.branchesPerdidos}/${arquivo.branchesTotal} | Cobertura: ${arquivo.branchesPercentual}%`);
    });
}

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverLinha(pc.red(`Erro ao analisar branches do frontend: ${mensagem}`));
        process.exitCode = 1;
    });
}

export {
    principal,
};
