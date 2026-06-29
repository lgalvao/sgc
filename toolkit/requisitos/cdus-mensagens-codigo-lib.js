import path from "node:path";
import {lerArquivo} from "./cdus-lib.js";

const CAMINHO_MENSAGENS_BACKEND = "backend/src/main/java/sgc/comum/Mensagens.java";
const CAMINHO_ASSUNTOS_BACKEND = "backend/src/main/java/sgc/alerta/AssuntosNotificacao.java";
const CAMINHOS_CONSTANTES_FRONTEND = [
    "frontend/src/constants/notificacoes.ts",
    "frontend/src/constants/textos-subprocesso.ts",
    "frontend/src/constants/textos-mapa.ts",
    "frontend/src/constants/textos-diagnostico.ts",
    "frontend/src/constants/textos-processo.ts"
];

const STOPWORDS = new Set(["a", "ao", "as", "da", "das", "de", "do", "dos", "e", "em", "na", "no", "o", "os", "para"]);
const PREFIXOS_UI_EXCLUIDOS = [
    "BOTAO_",
    "BTN_",
    "LABEL_",
    "COLUNA_",
    "TITULO",
    "EMPTY_",
    "VAZIO_",
    "CARREGANDO",
    "ERRO_",
    "MODAL_",
    "INFO_",
    "ESCALA_",
    "NOTA_",
    "DETALHE_"
];
const CHAVES_MENSAGEM_EXPLICITAS = new Set([
    "ACEITE_REGISTRADO",
    "DEVOLUCAO_REALIZADA",
    "HOMOLOGACAO_EFETIVADA",
    "PROCESSO_ALTERADO",
    "PROCESSO_CRIADO",
    "PROCESSO_FINALIZADO",
    "PROCESSO_INICIADO"
]);

function extrairConstantesJava(texto, caminhoRelativo) {
    const resultados = [];
    for (const match of texto.matchAll(/public static final String\s+([A-Z0-9_]+)\s*=\s*"([^"]+)";/g)) {
        resultados.push({
            chave: match[1],
            texto: match[2],
            origem: `${caminhoRelativo}#${match[1]}`
        });
    }
    return resultados;
}

function extrairAssuntosBackend(texto, caminhoRelativo) {
    const resultados = [];

    for (const match of texto.matchAll(/return\s+"([^"]+)";/g)) {
        const literal = match[1];
        if (literal.startsWith("SGC: ")) {
            resultados.push({
                chave: "ASSUNTO_DIRETO",
                texto: literal,
                origem: `${caminhoRelativo}#return`
            });
        }
    }

    const blocoSubprocesso = texto.match(/public static String subprocesso\([\s\S]*?return incluirSigla[\s\S]*?\n {4}}/);
    const textoSubprocesso = blocoSubprocesso?.[0] ?? "";

    for (const match of textoSubprocesso.matchAll(/case\s+([A-Z0-9_, ]+)\s*->\s*"([^"]+)"(?:\s*\.\s*formatted\([^)]+\))?;/g)) {
        const casos = match[1]
            .split(",")
            .map(item => item.trim())
            .filter(Boolean);
        const literal = match[2];

        if (literal.startsWith("SGC: ")) {
            for (const caso of casos) {
                resultados.push({
                    chave: caso,
                    texto: literal,
                    origem: `${caminhoRelativo}#${caso}`
                });
            }
            continue;
        }

        for (const caso of casos) {
            const textoBase = literal.replace("%s", ":SIGLA_UNIDADE_SUBPROCESSO:");
            resultados.push({
                chave: caso,
                texto: `SGC: ${textoBase}`,
                origem: `${caminhoRelativo}#${caso}`
            });
            resultados.push({
                chave: `${caso}_SUPERIOR`,
                texto: `SGC: ${textoBase} - :SIGLA_UNIDADE_SUBPROCESSO:`,
                origem: `${caminhoRelativo}#${caso}`
            });
        }
    }

    for (const match of texto.matchAll(/return\s+"([^"]+)"\.formatted\([^)]+\);/g)) {
        const literal = match[1];
        if (literal.startsWith("SGC: ")) {
            resultados.push({
                chave: "ASSUNTO_FORMATADO",
                texto: literal.replaceAll("%s", ":VALOR:"),
                origem: `${caminhoRelativo}#formatted`
            });
        }
    }

    return resultados;
}

function extrairConstantesTypescript(texto, caminhoRelativo) {
    const resultados = [];
    for (const match of texto.matchAll(/([A-Z0-9_]+):\s*(?:"([^"]+)"|'([^']+)')/g)) {
        resultados.push({
            chave: match[1],
            texto: match[2] ?? match[3],
            origem: `${caminhoRelativo}#${match[1]}`
        });
    }
    return resultados;
}

function normalizarTextoComparacao(texto) {
    return texto
        .normalize("NFD")
        .replaceAll(/[\u0300-\u036f]/g, "")
        .replaceAll(/:[A-Z0-9_]+:/g, " valor ")
        .replaceAll(/[`"'.,;:!?()[\]{}]/g, " ")
        .replaceAll(/\s+/g, " ")
        .trim()
        .toLowerCase();
}

function tokenizar(texto) {
    return normalizarTextoComparacao(texto)
        .split(" ")
        .filter(token => token.length > 1 && !STOPWORDS.has(token));
}

function bigramas(texto) {
    const base = ` ${normalizarTextoComparacao(texto)} `;
    const pares = [];
    for (let indice = 0; indice < base.length - 1; indice += 1) {
        pares.push(base.slice(indice, indice + 2));
    }
    return pares;
}

function calcularSimilaridade(a, b) {
    const tokensA = new Set(tokenizar(a));
    const tokensB = new Set(tokenizar(b));
    const intersecao = [...tokensA].filter(token => tokensB.has(token)).length;
    const uniao = new Set([...tokensA, ...tokensB]).size || 1;
    const scoreTokens = intersecao / uniao;

    const paresA = bigramas(a);
    const paresB = bigramas(b);
    const contagemB = new Map();
    for (const par of paresB) {
        contagemB.set(par, (contagemB.get(par) ?? 0) + 1);
    }
    let intersecaoBigramas = 0;
    for (const par of paresA) {
        const restante = contagemB.get(par) ?? 0;
        if (restante > 0) {
            intersecaoBigramas += 1;
            contagemB.set(par, restante - 1);
        }
    }
    const scoreBigramas = (2 * intersecaoBigramas) / ((paresA.length + paresB.length) || 1);
    return Number(((scoreTokens * 0.6) + (scoreBigramas * 0.4)).toFixed(3));
}

function indexarCanonicos(itens) {
    const indice = new Map();
    for (const item of itens) {
        const chave = normalizarTextoComparacao(item.texto);
        const lista = indice.get(chave) ?? [];
        lista.push(item);
        indice.set(chave, lista);
    }
    return indice;
}

function sugerirCanonicos(texto, canonicos, limite = 3) {
    return canonicos
        .map(item => ({...item, similaridade: calcularSimilaridade(texto, item.texto)}))
        .filter(item => item.similaridade >= 0.35)
        .sort((a, b) => b.similaridade - a.similaridade || a.texto.localeCompare(b.texto, "pt-BR"))
        .slice(0, limite);
}

function carregarMensagensCanonicas(base) {
    const itens = [];

    const caminhoBackend = path.join(base, CAMINHO_MENSAGENS_BACKEND);
    const constantesBackend = extrairConstantesJava(lerArquivo(caminhoBackend), CAMINHO_MENSAGENS_BACKEND);
    for (const item of constantesBackend) {
        if (item.chave.startsWith("HIST_")) {
            itens.push({...item, categoria: "descricao", grupo: "historico_backend"});
        }
        if (item.chave.startsWith("ALERTA_")) {
            itens.push({...item, categoria: "descricao", grupo: "alerta_backend"});
        }
    }

    const caminhoAssuntos = path.join(base, CAMINHO_ASSUNTOS_BACKEND);
    const assuntosBackend = extrairAssuntosBackend(lerArquivo(caminhoAssuntos), CAMINHO_ASSUNTOS_BACKEND);
    for (const item of assuntosBackend) {
        itens.push({...item, categoria: "assunto", grupo: "assunto_backend"});
    }

    for (const caminhoRelativo of CAMINHOS_CONSTANTES_FRONTEND) {
        const caminhoCompleto = path.join(base, caminhoRelativo);
        const constantes = extrairConstantesTypescript(lerArquivo(caminhoCompleto), caminhoRelativo);
        for (const item of constantes) {
            if (item.chave.startsWith("SUCESSO_")) {
                itens.push({...item, categoria: "toast", grupo: "sucesso_frontend"});
                itens.push({...item, categoria: "mensagem", grupo: "sucesso_frontend"});
                continue;
            }
            if (caminhoRelativo.endsWith("notificacoes.ts")) {
                itens.push({...item, categoria: "descricao", grupo: "notificacao_frontend"});
                continue;
            }
            if (item.chave.startsWith("PROCESSO_")) {
                itens.push({...item, categoria: "descricao", grupo: "movimentacao_frontend"});
            }
            const ehUiExcluida = PREFIXOS_UI_EXCLUIDOS.some(prefixo => item.chave.startsWith(prefixo));
            if (!ehUiExcluida && CHAVES_MENSAGEM_EXPLICITAS.has(item.chave)) {
                itens.push({...item, categoria: "toast", grupo: "resultado_frontend"});
                itens.push({...item, categoria: "mensagem", grupo: "resultado_frontend"});
            }
        }
    }

    return {
        itens,
        indice: indexarCanonicos(itens)
    };
}

export {
    calcularSimilaridade,
    carregarMensagensCanonicas,
    extrairAssuntosBackend,
    normalizarTextoComparacao,
    sugerirCanonicos
};
