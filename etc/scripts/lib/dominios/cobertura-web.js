import fs from "node:fs/promises";
import {resolverNaRaiz} from "../caminhos.js";

function calcularPercentualPorTotal(cobertos, total) {
    if (total <= 0) return 0;
    return Number(((cobertos / total) * 100).toFixed(2));
}

function extrairContagemCobertura(mapaCobertura = {}) {
    const total = Object.keys(mapaCobertura).length;
    const cobertos = Object.values(mapaCobertura).filter((valor) => valor > 0).length;
    return {total, cobertos};
}

function extrairContagemBranches(mapaBranches = {}) {
    const total = Object.values(mapaBranches).reduce((acumulado, valores) => acumulado + valores.length, 0);
    const cobertos = Object.values(mapaBranches).reduce(
        (acumulado, valores) => acumulado + valores.filter((valor) => valor > 0).length,
        0
    );
    return {total, cobertos};
}

function acumularTotaisCobertura(totais, chave, contagem) {
    totais[chave].total += contagem.total;
    totais[chave].cobertos += contagem.cobertos;
}

function criarResumoCobertura(cobertos, total) {
    return {
        cobertos,
        total,
        percentual: calcularPercentualPorTotal(cobertos, total)
    };
}

function normalizarCaminho(caminhoAbsolutoOuRelativo) {
    let relativo = caminhoAbsolutoOuRelativo.replace(/\\/g, "/");
    if (relativo.includes("frontend/src")) {
        relativo = relativo.substring(relativo.indexOf("frontend/src"));
    } else if (relativo.includes("src")) {
        relativo = relativo.substring(relativo.indexOf("src"));
    }
    return relativo;
}

function deveIgnorarArquivo(caminhoRelativo) {
    return caminhoRelativo.includes("node_modules")
        || caminhoRelativo.includes(".spec.ts")
        || caminhoRelativo.includes(".test.ts");
}

async function extrairCoberturaFrontend(caminhoRelativo = "frontend/coverage/coverage-final.json") {
    const caminhoJson = resolverNaRaiz(caminhoRelativo);
    let conteudo;
    try {
        conteudo = await fs.readFile(caminhoJson, "utf-8");
    } catch {
        throw new Error(`Relatório V8 (coverage-final.json) não encontrado em ${caminhoRelativo}`);
    }

    const cobertura = JSON.parse(conteudo);
    const arquivos = [];
    const totais = {
        statements: {cobertos: 0, total: 0},
        branches: {cobertos: 0, total: 0},
        functions: {cobertos: 0, total: 0},
        lines: {cobertos: 0, total: 0}
    };

    for (const [arquivoPath, dados] of Object.entries(cobertura)) {
        const caminhoNorm = normalizarCaminho(arquivoPath);
        if (deveIgnorarArquivo(caminhoNorm)) continue;

        const statements = extrairContagemCobertura(dados.s ?? {});
        const functions = extrairContagemCobertura(dados.f ?? {});
        const branches = extrairContagemBranches(dados.b ?? {});
        const lines = {
            total: Object.keys(dados.statementMap ?? {}).length,
            cobertos: statements.cobertos
        };

        acumularTotaisCobertura(totais, "statements", statements);
        acumularTotaisCobertura(totais, "functions", functions);
        acumularTotaisCobertura(totais, "branches", branches);
        acumularTotaisCobertura(totais, "lines", lines);

        arquivos.push({
            arquivo: caminhoNorm,
            statementsPercentual: calcularPercentualPorTotal(statements.cobertos, statements.total),
            statementsCobertos: statements.cobertos,
            statementsTotal: statements.total,
            branchesPercentual: calcularPercentualPorTotal(branches.cobertos, branches.total),
            functionsPercentual: calcularPercentualPorTotal(functions.cobertos, functions.total),
            linesPercentual: calcularPercentualPorTotal(lines.cobertos, lines.total)
        });
    }

    return {
        statements: criarResumoCobertura(totais.statements.cobertos, totais.statements.total),
        branches: criarResumoCobertura(totais.branches.cobertos, totais.branches.total),
        functions: criarResumoCobertura(totais.functions.cobertos, totais.functions.total),
        lines: criarResumoCobertura(totais.lines.cobertos, totais.lines.total),
        arquivos: arquivos.sort((a, b) => a.linesPercentual - b.linesPercentual)
    };
}

export {
    extrairCoberturaFrontend,
    normalizarCaminho,
    deveIgnorarArquivo
};
