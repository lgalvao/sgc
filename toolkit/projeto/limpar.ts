import path from "node:path";
import {access, rm} from "node:fs/promises";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const PADROES_LIMPEZA_SGC = [
    "backend/build",
    "frontend/coverage",
    "frontend/dist",
    "coverage",
    "playwright-report",
    "test-results",
    "toolkit/qualidade/artefatos",
    "toolkit/qualidade/semgrep/latest",
    "analise-testes.md",
    "analise-testes.json",
    "complexity-ranking.md",
    "cobertura_lacunas.json",
    "frontend-backend-validation-comparison.md",
    "mensagens-analise.md",
    "mensagens-extraidas.json",
    "backend-coverage-auditoria.md",
    "frontend-coverage-auditoria.md",
    "null-checks-analysis.md",
    "null-checks-audit.txt",
    "plano-100-cobertura.md",
    "plano-cobertura-backend.md",
    "priorizacao-testes.md",
    "relatorio-testes.md",
    "unit-test-report.md"
];

interface OpcoesLimpeza {
    base?: string;
    confirmar?: boolean;
    json?: boolean;
    padroes?: readonly string[];
}

interface ResultadoLimpeza {
    diretorioBase: string;
    modo: "simular" | "executar";
    total: number;
    itens: string[];
}

async function existeCaminho(caminho: string): Promise<boolean> {
    try {
        await access(caminho);
        return true;
    } catch {
        return false;
    }
}

async function resolverItens(base: string, padroes: readonly string[]): Promise<string[]> {
    const encontrados = new Set<string>();

    for (const padrao of padroes) {
        if (!padrao.includes("*")) {
            const absoluto = path.resolve(base, padrao);
            if (await existeCaminho(absoluto)) {
                encontrados.add(absoluto);
            }
            continue;
        }

        const itens = await globby(padrao, {
            cwd: base,
            absolute: true,
            dot: true,
            onlyFiles: false
        });

        for (const item of itens) {
            encontrados.add(item);
        }
    }

    return [...encontrados].toSorted((a, b) => a.localeCompare(b));
}

function relativo(base: string, absoluto: string): string {
    return path.relative(base, absoluto).replaceAll("\\", "/");
}

function imprimirHumano(base: string, itens: string[], modo: ResultadoLimpeza["modo"]): void {
    imprimirCabecalho("Limpeza do projeto", modo === "executar"
        ? "Removendo artefatos gerados pelo toolkit e ferramentas de qualidade."
        : "Prévia dos artefatos gerados pelo toolkit e ferramentas de qualidade.");
    escreverLinha("");

    if (itens.length === 0) {
        escreverLinha("Nenhum artefato elegível para limpeza foi encontrado.");
        return;
    }

    for (const item of itens) {
        escreverLinha(`- ${relativo(base, item)}`);
    }

    escreverLinha("");
    escreverLinha(`Total: ${itens.length} item(ns).`);
}

async function executarLimpeza(opcoes: OpcoesLimpeza = {}): Promise<ResultadoLimpeza> {
    const base = opcoes.base ? path.resolve(opcoes.base) : resolverNaRaiz();
    const itens = await resolverItens(base, opcoes.padroes ?? PADROES_LIMPEZA_SGC);
    const modo: ResultadoLimpeza["modo"] = opcoes.confirmar ? "executar" : "simular";

    if (opcoes.confirmar) {
        for (const item of itens) {
            await rm(item, {recursive: true, force: true});
        }
    }

    const saida = {
        diretorioBase: base,
        modo,
        total: itens.length,
        itens: itens.map((item) => relativo(base, item))
    };

    if (opcoes.json) {
        imprimirJson(saida);
    } else {
        imprimirHumano(base, itens, modo);
        if (!opcoes.confirmar) {
            escreverLinha("Use `--confirmar` para remover de fato.");
        }
    }

    return saida;
}

export {
    executarLimpeza,
    PADROES_LIMPEZA_SGC
};
