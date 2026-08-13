import path from "node:path";
import {access, rm} from "node:fs/promises";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {resolverCaminhoConfigurado} from "../lib/configuracao.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

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

function relativo(base: string, absoluto: string): string {
    return path.relative(base, absoluto).replaceAll("\\", "/");
}

function obterPadroesLimpezaPadrao(base: string): string[] {
    const diretorioBackend = resolverCaminhoConfigurado("backend", base);
    const diretorioFrontend = resolverCaminhoConfigurado("frontend", base);
    const diretorioArtefatos = resolverCaminhoConfigurado("artefatosQualidade", base);
    return [
        relativo(base, path.join(diretorioBackend, "build")),
        relativo(base, path.join(diretorioFrontend, "coverage")),
        relativo(base, path.join(diretorioFrontend, "dist")),
        "coverage",
        "playwright-report",
        "test-results",
        relativo(base, diretorioArtefatos),
        "analise-testes.md",
        "analise-testes.json",
        "priorizacao-testes.md",
        "backend-coverage-auditoria.md",
        "frontend-coverage-auditoria.md"
    ];
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
    const itens = await resolverItens(base, opcoes.padroes ?? obterPadroesLimpezaPadrao(base));
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
    obterPadroesLimpezaPadrao
};
