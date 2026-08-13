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
    instrucoesPercentual: number;
    instrucoesCobertas: number;
    instrucoesTotal: number;
    ramificacoesPercentual: number;
    ramificacoesTotal: number;
    funcoesPercentual: number;
    funcoesTotal: number;
    linhasPercentual: number;
}

export interface OpcoesCoberturaFrontend {
    diretorioBase?: string;
}

export interface ResultadoCoberturaFrontend {
    instrucoes: ResumoCobertura;
    ramificacoes: ResumoCobertura;
    funcoes: ResumoCobertura;
    linhas: ResumoCobertura;
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

function extrairContagemRamificacoes(mapaRamificacoes: Record<string, number[]> = {}): {total: number; cobertos: number} {
    const total = Object.values(mapaRamificacoes).reduce((acumulado, valores) => acumulado + valores.length, 0);
    const cobertos = Object.values(mapaRamificacoes).reduce(
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
        instrucoes: {cobertos: 0, total: 0},
        ramificacoes: {cobertos: 0, total: 0},
        funcoes: {cobertos: 0, total: 0},
        linhas: {cobertos: 0, total: 0}
    };

    for (const [arquivoPath, dados] of Object.entries(cobertura)) {
        const caminhoNorm = normalizarCaminho(arquivoPath, diretorioBase);
        if (deveIgnorarArquivo(caminhoNorm)) continue;

        const instrucoes = extrairContagemCobertura(dados.s ?? {});
        const funcoes = extrairContagemCobertura(dados.f ?? {});
        const ramificacoes = extrairContagemRamificacoes(dados.b ?? {});
        const linhas = {
            total: Object.keys(dados.statementMap ?? {}).length,
            cobertos: instrucoes.cobertos
        };

        acumularTotaisCobertura(totais, "instrucoes", instrucoes);
        acumularTotaisCobertura(totais, "funcoes", funcoes);
        acumularTotaisCobertura(totais, "ramificacoes", ramificacoes);
        acumularTotaisCobertura(totais, "linhas", linhas);

        arquivos.push({
            arquivo: caminhoNorm,
            instrucoesPercentual: calcularPercentualPorTotal(instrucoes.cobertos, instrucoes.total),
            instrucoesCobertas: instrucoes.cobertos,
            instrucoesTotal: instrucoes.total,
            ramificacoesPercentual: calcularPercentualPorTotal(ramificacoes.cobertos, ramificacoes.total),
            ramificacoesTotal: ramificacoes.total,
            funcoesPercentual: calcularPercentualPorTotal(funcoes.cobertos, funcoes.total),
            funcoesTotal: funcoes.total,
            linhasPercentual: calcularPercentualPorTotal(linhas.cobertos, linhas.total)
        });
    }

    return {
        instrucoes: criarResumoCobertura(totais.instrucoes.cobertos, totais.instrucoes.total),
        ramificacoes: criarResumoCobertura(totais.ramificacoes.cobertos, totais.ramificacoes.total),
        funcoes: criarResumoCobertura(totais.funcoes.cobertos, totais.funcoes.total),
        linhas: criarResumoCobertura(totais.linhas.cobertos, totais.linhas.total),
        arquivos: arquivos.toSorted((a, b) => a.linhasPercentual - b.linhasPercentual)
    };
}

export {
    extrairCoberturaFrontend
};
