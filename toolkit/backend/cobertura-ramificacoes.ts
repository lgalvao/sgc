import path from "node:path";
import pc from "picocolors";
import {lerOpcao} from "../lib/cli-opcoes.js";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {ehEntradaPrincipal} from "../lib/execucao.js";
import {extrairCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import type {ClasseCobertura, ResultadoCoberturaJacoco} from "../lib/dominios/cobertura-java.js";
import {escreverErro, escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";
import {exibirAjudaComando} from "../lib/cli-ajuda.js";

interface ClasseRamificacoes {
    nome: string;
    branchesPerdidos: number;
    totalBranches: number;
    branchesPercentual: number;
    branchesPerdidosLista: string[];
}

interface ResultadoRamificacoes {
    status: "ok";
    timestamp: string;
    totais: ResultadoCoberturaJacoco["branches"];
    classes: ClasseRamificacoes[];
}

function resumirClasse(classe: ClasseCobertura): ClasseRamificacoes {
    return {
        nome: classe.nome,
        branchesPerdidos: classe.branchesPerdidos,
        totalBranches: classe.totalBranches,
        branchesPercentual: classe.branchesPercentual,
        branchesPerdidosLista: classe.branchesPerdidosLista
    };
}

async function principal(argumentos: string[] = process.argv.slice(2)): Promise<void> {
    const emitirJson = argumentos.includes("--json");
    const exibirAjuda = argumentos.includes("--help") || argumentos.includes("-h");

    if (exibirAjuda) {
        exibirAjudaComando({
            comandoSgc: "backend cobertura ramificacoes",
            scriptDireto: "backend/cobertura-ramificacoes.ts",
            descricao: "Lista classes backend com ramificacoes perdidas no relatorio JaCoCo.",
            opcoes: [
                "--json            Saída estruturada em JSON.",
                "--limite <n>      Limita a quantidade de classes exibidas. Padrão: 20.",
                "--filtro <texto>  Filtra por nome de classe/pacote.",
                "--arquivo <xml>   Usa um relatório JaCoCo específico.",
                "--base <diretorio> Resolve o relatório relativo a outra base."
            ]
        });
        return;
    }

    const diretorioBase = path.resolve(lerOpcao(argumentos, "--base", resolverNaRaiz()) ?? resolverNaRaiz());
    const arquivo = lerOpcao(argumentos, "--arquivo", "") ?? "";
    const limite = Number.parseInt(lerOpcao(argumentos, "--limite", "20") ?? "20", 10);
    const filtro = lerOpcao(argumentos, "--filtro", "") || null;
    const coleta = await extrairCoberturaJacoco(arquivo || undefined, {
        diretorioBase,
        incluirSemLacunas: true,
        aplicarExclusoes: true,
        filtro
    });

    const classes = coleta.classes
        .filter((classe) => classe.branchesPerdidos > 0)
        .toSorted((a, b) => b.branchesPerdidos - a.branchesPerdidos || a.branchesPercentual - b.branchesPercentual)
        .slice(0, limite)
        .map(resumirClasse);

    const resultado: ResultadoRamificacoes = {
        status: "ok",
        timestamp: new Date().toISOString(),
        totais: coleta.branches,
        classes
    };

    if (emitirJson) {
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

if (ehEntradaPrincipal(import.meta.url)) {
    principal().catch((erro) => {
        const mensagem = erro instanceof Error ? erro.message : String(erro);
        escreverErro(`${pc.red(`Erro ao analisar branches do backend: ${mensagem}`)}\n`);
        process.exitCode = 1;
    });
}

export {
    principal
};
