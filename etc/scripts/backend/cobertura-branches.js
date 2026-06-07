#!/usr/bin/env node
import pc from "picocolors";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {imprimirCabecalho, imprimirJson, escreverLinha} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

async function main() {
    const args = process.argv.slice(2);
    const jsonMode = args.includes("--json");
    const helpMode = args.includes("--help") || args.includes("-h");
    const limite = Number.parseInt(args.find((arg) => arg.startsWith("--limite="))?.split("=")[1] ?? "20", 10);
    const filtro = args.find((arg) => arg.startsWith("--filtro="))?.split("=")[1] ?? null;

    if (helpMode) {
        exibirAjudaComando({
            comandoSgc: "backend cobertura branches",
            scriptDireto: "backend/cobertura-branches.js",
            descricao: "Lista classes backend com branches perdidos no relatório JaCoCo.",
            opcoes: [
                "--json            Saída estruturada em JSON.",
                "--limite=N        Limita a quantidade de classes exibidas. Padrão: 20.",
                "--filtro=texto    Filtra por nome de classe/pacote."
            ]
        });
        process.exit(0);
    }

    const coleta = await extrairCoberturaJacoco(undefined, {
        incluirSemLacunas: true,
        aplicarExclusoes: true,
        filtro
    });

    const classes = coleta.classes
        .filter((classe) => classe.branchesPerdidos > 0)
        .sort((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite)
        .map((classe) => ({
            nome: classe.nome,
            branchesPerdidos: classe.branchesPerdidos,
            totalBranches: classe.totalBranches,
            branchesPercentual: classe.branchesPercentual,
            branchesPerdidosLista: classe.branchesPerdidosLista
        }));

    const resultado = {
        status: "ok",
        timestamp: new Date().toISOString(),
        totais: coleta.branches,
        classes
    };

    if (jsonMode) {
        imprimirJson(resultado);
        return;
    }

    imprimirCabecalho("COBERTURA DE BRANCHES BACKEND");
    escreverLinha(`Cobertura global de branches: ${pc.bold(`${coleta.branches.percentual}%`)} (${coleta.branches.cobertos}/${coleta.branches.cobertos + coleta.branches.perdidos})`);
    escreverLinha("");

    if (classes.length === 0) {
        escreverLinha(pc.green("Nenhuma lacuna de branches encontrada nas classes auditadas."));
        return;
    }

    escreverLinha(pc.bold(pc.underline(`TOP ${classes.length} CLASSES COM LACUNAS DE BRANCHES:`)));
    classes.forEach((classe, indice) => {
        escreverLinha(`${indice + 1}. ${pc.bold(classe.nome)}`);
        escreverLinha(`   Branches perdidos: ${classe.branchesPerdidos}/${classe.totalBranches} | Cobertura: ${classe.branchesPercentual}%`);
        if (classe.branchesPerdidosLista.length > 0) {
            escreverLinha(`   Linhas: ${pc.dim(classe.branchesPerdidosLista.join(", "))}`);
        }
    });
}

main().catch((erro) => {
    console.error(pc.red(`Erro ao analisar branches do backend: ${erro.message}`));
    process.exit(1);
});
