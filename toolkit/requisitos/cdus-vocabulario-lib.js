import fs from "node:fs";
import path from "node:path";

const PERFIS_CANONICOS = new Set([
    "ADMIN",
    "GESTOR",
    "CHEFE",
    "SERVIDOR"
]);

const TIPOS_PROCESSO_CANONICOS = new Set([
    "Mapeamento",
    "Revisão",
    "Diagnóstico"
]);

function carregarSituacoesCanonicas(base = process.cwd()) {
    const caminho = path.join(base, "specs", "_intro_3_situacoes.md");
    const texto = fs.readFileSync(caminho, "utf8");
    const situacoes = new Set();

    for (const linha of texto.split(/\r?\n/)) {
        const correspondencia = linha.match(/^\s*-\s+\*\*([^*]+)\*\*:/);
        if (correspondencia) {
            situacoes.add(correspondencia[1].trim());
        }
    }

    return situacoes;
}

function normalizarTexto(texto) {
    return texto
        .normalize("NFD")
        .replaceAll(/\p{Diacritic}/gu, "")
        .toLowerCase()
        .replaceAll(/[^a-z0-9]+/g, " ")
        .trim();
}

function similaridadeBasica(a, b) {
    const compactoA = normalizarTexto(a).replaceAll(" ", "");
    const compactoB = normalizarTexto(b).replaceAll(" ", "");
    if (compactoA === compactoB) {
        return 1;
    }

    const aa = new Set(normalizarTexto(a).split(" ").filter(Boolean));
    const bb = new Set(normalizarTexto(b).split(" ").filter(Boolean));
    if (aa.size === 0 || bb.size === 0) {
        return 0;
    }

    const interseccao = [...aa].filter(item => bb.has(item)).length;
    return interseccao / Math.max(aa.size, bb.size);
}

function sugerirCanonico(valor, canonicos) {
    let melhor = null;
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
