import fs from "node:fs/promises";
import path from "node:path";
import {DIRETORIO_RAIZ} from "../caminhos.js";
import {resolverCaminhoConfigurado} from "../configuracao.js";

export interface DadosCoberturaV8 {
    s?: Record<string, number>;
    f?: Record<string, number>;
    b?: Record<string, number[]>;
    statementMap?: Record<string, unknown>;
}

export interface ResumoCobertura {
    cobertos: number;
    total: number;
    percentual: number;
}

export interface ArquivoCobertura {
    arquivo: string;
    statementsPercentual: number;
    statementsCobertos: number;
    statementsTotal: number;
    branchesPercentual: number;
    branchesTotal: number;
    functionsPercentual: number;
    functionsTotal: number;
    linesPercentual: number;
}

export interface OpcoesCoberturaFrontend {
    diretorioBase?: string;
}

export interface ResultadoCoberturaFrontend {
    statements: ResumoCobertura;
    branches: ResumoCobertura;
    functions: ResumoCobertura;
    lines: ResumoCobertura;
    arquivos: ArquivoCobertura[];
}

function calcularPercentualPorTotal(cobertos: number, total: number): number {
    if (total <= 0) return 0;
    return Number(((cobertos / total) * 100).toFixed(2));
}

function extrairContagemCobertura(mapaCobertura: Record<string, number> = {}): {total: number; cobertos: number} {
    const total = Object.keys(mapaCobertura).length;
    const cobertos = Object.values(mapaCobertura).filter((valor) => valor > 0).length;
    return {total, cobertos};
}

function extrairContagemBranches(mapaBranches: Record<string, number[]> = {}): {total: number; cobertos: number} {
    const total = Object.values(mapaBranches).reduce((acumulado, valores) => acumulado + valores.length, 0);
    const cobertos = Object.values(mapaBranches).reduce(
        (acumulado, valores) => acumulado + valores.filter((valor) => valor > 0).length,
        0
    );
    return {total, cobertos};
}

function acumularTotaisCobertura(
    totais: Record<string, {cobertos: number; total: number}>,
    chave: string,
    contagem: {cobertos: number; total: number}
): void {
    totais[chave].total += contagem.total;
    totais[chave].cobertos += contagem.cobertos;
}

function criarResumoCobertura(cobertos: number, total: number): ResumoCobertura {
    return {
        cobertos,
        total,
        percentual: calcularPercentualPorTotal(cobertos, total)
    };
}

function normalizarCaminho(caminhoAbsolutoOuRelativo: string, diretorioBase: string | null = null): string {
    if (!diretorioBase) {
        return caminhoAbsolutoOuRelativo.replace(/\\/g, "/");
    }

    const caminhoAbsoluto = path.isAbsolute(caminhoAbsolutoOuRelativo)
        ? caminhoAbsolutoOuRelativo
        : path.resolve(diretorioBase, caminhoAbsolutoOuRelativo);
    return path.relative(diretorioBase, caminhoAbsoluto).replace(/\\/g, "/");
}

function deveIgnorarArquivo(caminhoRelativo: string): boolean {
    return caminhoRelativo.includes("node_modules")
        || caminhoRelativo.includes(".spec.ts")
        || caminhoRelativo.includes(".test.ts");
}

async function extrairCoberturaFrontend(
    caminhoRelativo: string | null = null,
    opcoes: OpcoesCoberturaFrontend = {}
): Promise<ResultadoCoberturaFrontend> {
    const diretorioBase = opcoes.diretorioBase ?? DIRETORIO_RAIZ;
    const caminhoPadrao = resolverCaminhoConfigurado("coberturaFrontend", diretorioBase);
    const caminhoJson = caminhoRelativo
        ? (path.isAbsolute(caminhoRelativo) ? caminhoRelativo : path.resolve(diretorioBase, caminhoRelativo))
        : caminhoPadrao;
    let conteudo;
    try {
        conteudo = await fs.readFile(caminhoJson, "utf-8");
    } catch {
        throw new Error(`Relatorio V8 (coverage-final.json) nao encontrado em ${caminhoJson}`);
    }

    const cobertura = JSON.parse(conteudo) as Record<string, DadosCoberturaV8>;
    const arquivos: ArquivoCobertura[] = [];
    const totais = {
        statements: {cobertos: 0, total: 0},
        branches: {cobertos: 0, total: 0},
        functions: {cobertos: 0, total: 0},
        lines: {cobertos: 0, total: 0}
    };

    for (const [arquivoPath, dados] of Object.entries(cobertura)) {
        const caminhoNorm = normalizarCaminho(arquivoPath, diretorioBase);
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
            branchesTotal: branches.total,
            functionsPercentual: calcularPercentualPorTotal(functions.cobertos, functions.total),
            functionsTotal: functions.total,
            linesPercentual: calcularPercentualPorTotal(lines.cobertos, lines.total)
        });
    }

    return {
        statements: criarResumoCobertura(totais.statements.cobertos, totais.statements.total),
        branches: criarResumoCobertura(totais.branches.cobertos, totais.branches.total),
        functions: criarResumoCobertura(totais.functions.cobertos, totais.functions.total),
        lines: criarResumoCobertura(totais.lines.cobertos, totais.lines.total),
        arquivos: arquivos.toSorted((a, b) => a.linesPercentual - b.linesPercentual)
    };
}

export {
    extrairCoberturaFrontend
};
