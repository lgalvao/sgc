#!/usr/bin/env node
import pc from "picocolors";
import {extrairCoberturaFrontend} from "../lib/dominios/cobertura-web.js";
import {imprimirCabecalho, imprimirJson, escreverLinha} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

function calcularBranchesPerdidos(arquivo) {
    return Math.max(0, arquivo.branchesTotal - Math.round((arquivo.branchesPercentual / 100) * arquivo.branchesTotal));
}

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");
    const limite = Number.parseInt(args.find((arg) => arg.startsWith("--limite="))?.split("=")[1] ?? "20", 10);

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "frontend cobertura branches",
            scriptDireto: "frontend/cobertura-branches.js",
            descricao: "Lista lacunas de cobertura de branches no frontend por arquivo.",
            opcoes: [
                "--json          Saída estruturada em JSON.",
                "--limite=N      Limita a quantidade de arquivos exibidos. Padrão: 20."
            ]
        });
        process.exit(0);
    }

    const coleta = await extrairCoberturaFrontend();
    const arquivos = coleta.arquivos
        .map((arquivo) => ({
            arquivo: arquivo.arquivo,
            branchesTotal: arquivo.branchesTotal,
            branchesPercentual: arquivo.branchesPercentual,
            branchesPerdidos: calcularBranchesPerdidos(arquivo)
        }))
        .filter((arquivo) => arquivo.branchesPerdidos > 0)
        .sort((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite);

    const resultado = {
        status: "ok",
        timestamp: new Date().toISOString(),
        totais: coleta.branches,
        arquivos
    };

    if (jsonMode) {
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

main().catch((erro) => {
    console.error(pc.red(`Erro ao analisar branches do frontend: ${erro.message}`));
    process.exit(1);
});
