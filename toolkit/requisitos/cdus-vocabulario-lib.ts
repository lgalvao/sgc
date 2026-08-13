import fs from "node:fs";
import path from "node:path";

const PERFIS_CANONICOS = new Set<string>([
    "ADMIN",
    "GESTOR",
    "CHEFE",
    "SERVIDOR"
]);

const TIPOS_PROCESSO_CANONICOS = new Set<string>([
    "Mapeamento",
    "Revisão",
    "Diagnóstico"
]);

function carregarSituacoesCanonicas(base: string = process.cwd()): Set<string> {
    const caminho = path.join(base, "specs", "intro_3_situacoes.md");
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
    PERFIS_CANONICOS,
    TIPOS_PROCESSO_CANONICOS,
    carregarSituacoesCanonicas,
    sugerirCanonico
};
