import fs from "node:fs";
import path from "node:path";
import {carregarConfiguracao} from "../lib/configuracao.js";
import {DIRETORIO_RAIZ} from "../lib/caminhos.js";

interface VocabularioCanonico {
    perfis: Set<string>;
    tiposProcesso: Set<string>;
}

function obterVocabularioCanonico(base: string = DIRETORIO_RAIZ): VocabularioCanonico {
    const configuracao = carregarConfiguracao(base).requisitos.cdus.vocabulario;
    return {
        perfis: new Set(configuracao.perfisCanonicos),
        tiposProcesso: new Set(configuracao.tiposProcessoCanonicos)
    };
}

function carregarSituacoesCanonicas(base: string = DIRETORIO_RAIZ): Set<string> {
    const arquivo = carregarConfiguracao(base).requisitos.cdus.vocabulario.arquivoSituacoesCanonicas;
    const caminho = path.resolve(base, arquivo);
    if (!fs.existsSync(caminho)) {
        return new Set<string>();
    }
    const texto = fs.readFileSync(caminho, "utf8");
    const situacoes = new Set<string>();

    for (const linha of texto.split(/\r?\n/)) {
        const correspondencia = linha.match(/^\s*-\s+\*\*([^*]+)\*\*:/);
        if (correspondencia) {
            situacoes.add(correspondencia[1].trim());
        }
    }

    return situacoes;
}

function normalizarTexto(texto: string): string {
    return texto
        .normalize("NFD")
        .replaceAll(/\p{Diacritic}/gu, "")
        .toLowerCase()
        .replaceAll(/[^a-z0-9]+/g, " ")
        .trim();
}

function similaridadeBasica(a: string, b: string): number {
    const compactoA = normalizarTexto(a).replaceAll(" ", "");
    const compactoB = normalizarTexto(b).replaceAll(" ", "");
    if (compactoA === compactoB) {
        return 1;
    }

    const conjuntoA = new Set(normalizarTexto(a).split(" ").filter(Boolean));
    const conjuntoB = new Set(normalizarTexto(b).split(" ").filter(Boolean));
    if (conjuntoA.size === 0 || conjuntoB.size === 0) {
        return 0;
    }

    const interseccao = [...conjuntoA].filter(item => conjuntoB.has(item)).length;
    return interseccao / Math.max(conjuntoA.size, conjuntoB.size);
}

function sugerirCanonico(valor: string, canonicos: Iterable<string>): string | null {
    let melhor: string | null = null;
    let melhorScore = 0;

    for (const canonico of canonicos) {
        const score = similaridadeBasica(valor, canonico);
        if (score > melhorScore) {
            melhorScore = score;
            melhor = canonico;
        }
    }

    return melhorScore >= 0.5 ? melhor : null;
}

export {
    carregarSituacoesCanonicas,
    obterVocabularioCanonico,
    sugerirCanonico
};
