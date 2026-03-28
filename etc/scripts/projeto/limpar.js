import path from "node:path";
import fs from "fs-extra";
import {globby} from "globby";
import {resolverNaRaiz} from "../lib/caminhos.js";
import {escreverLinha, imprimirCabecalho, imprimirJson} from "../lib/saida.js";

const PADROES_LIMPEZA = [
    "backend/build",
    "frontend/coverage",
    "frontend/dist",
    "coverage",
    "playwright-report",
    "test-results",
    "etc/qa-dashboard/latest",
    "etc/qa-dashboard/runs",
    "etc/qa-dashboard/tmp",
    "analise-testes.md",
    "analise-testes.json",
    "complexity-ranking.md",
    "cobertura_lacunas.json",
    "frontend-backend-validation-comparison.md",
    "mensagens-analise.md",
    "mensagens-extraidas.json",
    "null-checks-analysis.md",
    "null-checks-audit.txt",
    "plano-100-cobertura.md",
    "plano-cobertura-backend.md",
    "priorizacao-testes.md",
    "relatorio-testes.md",
    "unit-test-report.md"
];

async function resolverItens(base) {
    const encontrados = new Set();

    for (const padrao of PADROES_LIMPEZA) {
        if (!padrao.includes("*")) {
            const absoluto = path.resolve(base, padrao);
            if (await fs.pathExists(absoluto)) {
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

    return [...encontrados].sort((a, b) => a.localeCompare(b));
}

function relativo(base, absoluto) {
    return path.relative(base, absoluto).replaceAll("\\", "/");
}

function imprimirHumano(base, itens, modo) {
    imprimirCabecalho("Limpeza do projeto", modo === "executar"
        ? "Removendo artefatos gerados pelo toolkit e ferramentas de QA."
        : "Prévia dos artefatos gerados pelo toolkit e ferramentas de QA.");
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

async function executarLimpeza(opcoes = {}) {
    const base = opcoes.base ? path.resolve(opcoes.base) : resolverNaRaiz();
    const itens = await resolverItens(base);
    const modo = opcoes.confirmar ? "executar" : "simular";

    if (opcoes.confirmar) {
        for (const item of itens) {
            await fs.remove(item);
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
    executarLimpeza
};
