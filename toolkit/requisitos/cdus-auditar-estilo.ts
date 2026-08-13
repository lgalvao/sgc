// Auditoria tipográfica dos casos de uso CDU.

import path from "node:path";
import {carregarConfiguracao} from "../biblioteca/configuracao.js";
import {lerArquivo, listarArquivosCdu, obterLinhas} from "./cdus-documentos-lib.js";

const REGEX_ASPAS_SIMPLES = /'([^'\n]+)'/g;
const REGEX_TITULO_UI_EM_ASPAS = /(?:título|titulo|subtítulo|subtitulo)\s*:?\s*"([^"\n]+)"/gi;
const REGEX_PLACEHOLDER_LEGADO = /\[[A-Z0-9_]+\]/g;

type RegraEstilo = "perfil_em_aspas_simples" | "ui_em_aspas_duplas" | "placeholder_legado";

interface AchadoEstilo {
    severidade: "aviso";
    regra: RegraEstilo;
    mensagem: string;
    linha: number | null;
}

interface ResultadoArquivoEstilo {
    arquivo: string;
    achados: AchadoEstilo[];
}

function adicionarAchado(
    achados: AchadoEstilo[],
    severidade: "aviso",
    regra: RegraEstilo,
    mensagem: string,
    linha: number | null = null
): void {
    achados.push({severidade, regra, mensagem, linha});
}

function encontrarAspasSimplesSuspeitas(linhas: string[], achados: AchadoEstilo[], perfis: Set<string>): void {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_ASPAS_SIMPLES)) {
            const valor = correspondencia[1].trim();
            if (perfis.has(valor)) {
                adicionarAchado(
                    achados,
                    "aviso",
                    "perfil_em_aspas_simples",
                    `Perfil em aspas simples: '${valor}'. Use \`${valor}\`.`,
                    indice + 1
                );
            }
        }
    });
}

function encontrarUiEmAspasDuplas(linhas: string[], achados: AchadoEstilo[]): void {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_TITULO_UI_EM_ASPAS)) {
            const valor = correspondencia[1].trim();
            const pareceMensagemLiteral = /[.?!]/.test(valor) || valor.split(/\s+/).length > 6;
            if (pareceMensagemLiteral) {
                continue;
            }

            adicionarAchado(
                achados,
                "aviso",
                "ui_em_aspas_duplas",
                `Possível título ou subtítulo de interface em aspas duplas: "${valor}". Considere usar crases.`,
                indice + 1
            );
        }
    });
}

function encontrarPlaceholdersLegados(linhas: string[], achados: AchadoEstilo[]): void {
    linhas.forEach((linha, indice) => {
        for (const correspondencia of linha.matchAll(REGEX_PLACEHOLDER_LEGADO)) {
            const valor = correspondencia[0];
            adicionarAchado(
                achados,
                "aviso",
                "placeholder_legado",
                `Placeholder no formato legado: ${valor}. Prefira \`${valor.replaceAll("[", ":").replaceAll("]", ":")}\`.`,
                indice + 1
            );
        }
    });
}

function auditarArquivo(caminhoArquivo: string, perfis: Set<string>): ResultadoArquivoEstilo {
    const texto = lerArquivo(caminhoArquivo);
    const linhas = obterLinhas(texto);
    const achados: AchadoEstilo[] = [];

    encontrarAspasSimplesSuspeitas(linhas, achados, perfis);
    encontrarUiEmAspasDuplas(linhas, achados);
    encontrarPlaceholdersLegados(linhas, achados);

    return {
        arquivo: caminhoArquivo,
        achados
    };
}

async function auditarEstilo(base: string, arquivosInformados?: string[]): Promise<{resumo: {
    base: string;
    totalArquivos: number;
    arquivosComAviso: number;
    avisos: number;
}; relatorio: ResultadoArquivoEstilo[]}> {
    const perfis = new Set(carregarConfiguracao(base).requisitos.cdus.estilo.perfisEmCrases);
    const arquivos = arquivosInformados ?? await listarArquivosCdu(base);
    const relatorio: ResultadoArquivoEstilo[] = arquivos.map(caminhoArquivo => {
        const resultado = auditarArquivo(caminhoArquivo, perfis);
        return {
            arquivo: path.relative(base, resultado.arquivo).replaceAll("\\", "/"),
            achados: resultado.achados
        };
    });

    const resumo = {
        base,
        totalArquivos: relatorio.length,
        arquivosComAviso: relatorio.filter(item => item.achados.length > 0).length,
        avisos: relatorio.flatMap(item => item.achados).length
    };

    return {resumo, relatorio};
}

export {
    auditarEstilo
};
