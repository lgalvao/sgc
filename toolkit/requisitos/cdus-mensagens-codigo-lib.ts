import fs from "node:fs";
import path from "node:path";
import {
    carregarConfiguracao,
    type FonteMensagensCodigo,
    type PoliticaMensagensCodigo
} from "../biblioteca/configuracao.js";
import {lerArquivo} from "./cdus-documentos-lib.js";

const PALAVRAS_VAZIAS_PADRAO = ["a", "ao", "as", "da", "das", "de", "do", "dos", "e", "em", "na", "no", "o", "os", "para"];

function resolverCaminhoFonte(base: string, fonte: FonteMensagensCodigo): string {
    return path.resolve(base, fonte.caminho);
}

function obterFontesMensagensCodigo(base: string): FonteMensagensCodigo[] {
    return carregarConfiguracao(base).requisitos.cdus.fontesMensagensCodigo;
}

function possuiFontesMensagensCanonicas(base: string, fontes: FonteMensagensCodigo[] = obterFontesMensagensCodigo(base)): boolean {
    return fontes.length > 0 && fontes.every(fonte => fs.existsSync(resolverCaminhoFonte(base, fonte)));
}

interface MensagemExtraida {
    chave: string;
    texto: string;
    origem: string;
}

export type CategoriaMensagem = "descricao" | "assunto" | "toast" | "mensagem";

export interface MensagemCanonica extends MensagemExtraida {
    categoria: CategoriaMensagem;
    grupo: string;
}

interface SugestaoCanonica extends MensagemCanonica {
    similaridade: number;
}

interface ResultadoMensagensCanonicas {
    itens: MensagemCanonica[];
    indice: Map<string, MensagemCanonica[]>;
}

function extrairConstantesJava(texto: string, caminhoRelativo: string): MensagemExtraida[] {
    const resultados: MensagemExtraida[] = [];
    for (const match of texto.matchAll(/public static final String\s+([A-Z0-9_]+)\s*=\s*"([^"]+)";/g)) {
        resultados.push({
            chave: match[1],
            texto: match[2],
            origem: `${caminhoRelativo}#${match[1]}`
        });
    }
    return resultados;
}

function extrairAssuntosJava(
    texto: string,
    caminhoRelativo: string,
    politica: PoliticaMensagensCodigo
): MensagemExtraida[] {
    const resultados: MensagemExtraida[] = [];
    const {assuntosJava} = politica;

    for (const match of texto.matchAll(/return\s+"([^"]+)";/g)) {
        const literal = match[1];
        if (literal.startsWith(assuntosJava.prefixo)) {
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

        if (literal.startsWith(assuntosJava.prefixo)) {
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
            const textoBase = literal.replace("%s", assuntosJava.marcadorSubprocesso);
            resultados.push({
                chave: caso,
                texto: `${assuntosJava.prefixo}${textoBase}`,
                origem: `${caminhoRelativo}#${caso}`
            });
            if (assuntosJava.sufixoSuperior) {
                resultados.push({
                    chave: `${caso}_SUPERIOR`,
                    texto: `${assuntosJava.prefixo}${textoBase}${assuntosJava.sufixoSuperior}`,
                    origem: `${caminhoRelativo}#${caso}`
                });
            }
        }
    }

    for (const match of texto.matchAll(/return\s+"([^"]+)"\.formatted\([^)]+\);/g)) {
        const literal = match[1];
        if (literal.startsWith(assuntosJava.prefixo)) {
            resultados.push({
                chave: "ASSUNTO_FORMATADO",
                texto: literal.replaceAll("%s", assuntosJava.marcadorFormatado),
                origem: `${caminhoRelativo}#formatted`
            });
        }
    }

    return resultados;
}

function extrairConstantesTypescript(texto: string, caminhoRelativo: string): MensagemExtraida[] {
    const resultados: MensagemExtraida[] = [];
    for (const match of texto.matchAll(/([A-Z0-9_]+):\s*(?:"([^"]+)"|'([^']+)')/g)) {
        resultados.push({
            chave: match[1],
            texto: match[2] ?? match[3],
            origem: `${caminhoRelativo}#${match[1]}`
        });
    }
    return resultados;
}

function normalizarTextoComparacao(texto: string): string {
    return texto
        .normalize("NFD")
        .replaceAll(/[\u0300-\u036f]/g, "")
        .replaceAll(/:[A-Z0-9_]+:/g, " valor ")
        .replaceAll(/[`"'.,;:!?()[\]{}]/g, " ")
        .replaceAll(/\s+/g, " ")
        .trim()
        .toLowerCase();
}

function tokenizar(texto: string, palavrasVazias: ReadonlySet<string>): string[] {
    return normalizarTextoComparacao(texto)
        .split(" ")
        .filter(token => token.length > 1 && !palavrasVazias.has(token));
}

function bigramas(texto: string): string[] {
    const base = ` ${normalizarTextoComparacao(texto)} `;
    const pares: string[] = [];
    for (let indice = 0; indice < base.length - 1; indice += 1) {
        pares.push(base.slice(indice, indice + 2));
    }
    return pares;
}

function calcularSimilaridade(a: string, b: string, palavrasVazias: ReadonlySet<string>): number {
    const tokensA = new Set(tokenizar(a, palavrasVazias));
    const tokensB = new Set(tokenizar(b, palavrasVazias));
    const intersecao = [...tokensA].filter(token => tokensB.has(token)).length;
    const uniao = new Set([...tokensA, ...tokensB]).size || 1;
    const pontuacaoTokens = intersecao / uniao;

    const paresA = bigramas(a);
    const paresB = bigramas(b);
    const contagemB = new Map<string, number>();
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
    const pontuacaoBigramas = (2 * intersecaoBigramas) / ((paresA.length + paresB.length) || 1);
    return Number(((pontuacaoTokens * 0.6) + (pontuacaoBigramas * 0.4)).toFixed(3));
}

function indexarCanonicos(itens: MensagemCanonica[]): Map<string, MensagemCanonica[]> {
    const indice = new Map<string, MensagemCanonica[]>();
    for (const item of itens) {
        const chave = normalizarTextoComparacao(item.texto);
        const lista = indice.get(chave) ?? [];
        lista.push(item);
        indice.set(chave, lista);
    }
    return indice;
}

function sugerirCanonicos(
    texto: string,
    canonicos: MensagemCanonica[],
    limite: number = 3,
    palavrasVazias: readonly string[] = PALAVRAS_VAZIAS_PADRAO
): SugestaoCanonica[] {
    const palavrasVaziasSet = new Set(palavrasVazias);
    return canonicos
        .map(item => ({...item, similaridade: calcularSimilaridade(texto, item.texto, palavrasVaziasSet)}))
        .filter(item => item.similaridade >= 0.35)
        .toSorted((a, b) => b.similaridade - a.similaridade || a.texto.localeCompare(b.texto, "pt-BR"))
        .slice(0, limite);
}

function adicionarConstantesTypescript(
    itens: MensagemCanonica[],
    texto: string,
    fonte: FonteMensagensCodigo,
    politica: PoliticaMensagensCodigo
): void {
    const constantes = extrairConstantesTypescript(texto, fonte.caminho);
    const {typescript} = politica;
    const chavesMensagem = new Set(politica.chavesMensagem);
    for (const item of constantes) {
        if (item.chave.startsWith(typescript.prefixoSucesso)) {
            for (const categoria of politica.categoriasChavesMensagem) {
                itens.push({...item, categoria, grupo: typescript.grupoSucesso});
            }
            continue;
        }
        if (fonte.tipo === "notificacoesTypescript") {
            itens.push({...item, categoria: "descricao", grupo: typescript.grupoNotificacao});
            continue;
        }
        if (item.chave.startsWith(typescript.prefixoMovimentacao)) {
            itens.push({...item, categoria: "descricao", grupo: typescript.grupoMovimentacao});
        }
        const ehUiExcluida = politica.prefixosUiExcluidos.some(prefixo => item.chave.startsWith(prefixo));
        if (!ehUiExcluida && chavesMensagem.has(item.chave)) {
            for (const categoria of politica.categoriasChavesMensagem) {
                itens.push({...item, categoria, grupo: typescript.grupoResultado});
            }
        }
    }
}

function carregarMensagensCanonicas(
    base: string,
    fontes: FonteMensagensCodigo[] = obterFontesMensagensCodigo(base),
    politica: PoliticaMensagensCodigo = carregarConfiguracao(base).requisitos.cdus.politicaMensagensCodigo
): ResultadoMensagensCanonicas {
    const itens: MensagemCanonica[] = [];

    for (const fonte of fontes) {
        const texto = lerArquivo(resolverCaminhoFonte(base, fonte));
        if (fonte.tipo === "mensagensJava") {
            for (const item of extrairConstantesJava(texto, fonte.caminho)) {
                for (const regra of politica.regrasJava) {
                    if (item.chave.startsWith(regra.prefixo)) {
                        itens.push({...item, categoria: regra.categoria, grupo: regra.grupo});
                    }
                }
            }
            continue;
        }
        if (fonte.tipo === "assuntosJava") {
            for (const item of extrairAssuntosJava(texto, fonte.caminho, politica)) {
                itens.push({...item, categoria: "assunto", grupo: politica.assuntosJava.grupo});
            }
            continue;
        }
        adicionarConstantesTypescript(itens, texto, fonte, politica);
    }

    return {
        itens,
        indice: indexarCanonicos(itens)
    };
}

export {
    carregarMensagensCanonicas,
    normalizarTextoComparacao,
    possuiFontesMensagensCanonicas,
    sugerirCanonicos
};
