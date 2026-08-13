import fs from "node:fs/promises";
import path from "node:path";
import {NOME_ARQUIVO_FOTOGRAFIA} from "../lib/qualidade.js";
import {extrairHotspotsQualidade} from "./coleta-leitores.js";
import type {ExecucaoQualidade} from "./coleta-execucao.js";

interface FotografiaColeta {
    versaoSchema: string;
    metadados: {
        geradoEm: string;
        perfilExecucao: string;
        duracaoTotalMs: number;
        git: Record<string, string>;
    };
    verificacoes: ExecucaoQualidade[];
    resumo: {
        statusGeral: "verde" | "vermelho";
        totais: {
            verificacoes: number;
            sucesso: number;
            falha: number;
        };
    };
    hotspots: Array<{
        nome: string;
        risco: number;
        origem: string;
    }>;
}

interface OpcoesFotografia {
    versaoSchema: string;
    perfilExecucao: string;
    inicio: number;
    verificacoes: ExecucaoQualidade[];
    git: Record<string, string>;
    agora?: Date;
}

interface DiretoriosFotografia {
    diretorioExecucao: string;
    diretorioMaisRecente: string;
}

function criarFotografiaColeta({
    versaoSchema,
    perfilExecucao,
    inicio,
    verificacoes,
    git,
    agora = new Date()
}: OpcoesFotografia): FotografiaColeta {
    const hotspots = verificacoes
        .flatMap((item) => extrairHotspotsQualidade(item.metricas).map((hotspot) => ({
            nome: hotspot.arquivo,
            risco: hotspot.score,
            origem: item.codigo
        })))
        .toSorted((a, b) => b.risco - a.risco)
        .slice(0, 20);

    return {
        versaoSchema,
        metadados: {
            geradoEm: agora.toISOString(),
            perfilExecucao,
            duracaoTotalMs: agora.getTime() - inicio,
            git
        },
        verificacoes,
        resumo: {
            statusGeral: verificacoes.some(v => v.status === "falha") ? "vermelho" : "verde",
            totais: {
                verificacoes: verificacoes.length,
                sucesso: verificacoes.filter(v => v.status === "sucesso").length,
                falha: verificacoes.filter(v => v.status === "falha").length
            }
        },
        hotspots
    };
}

async function prepararDiretoriosFotografia({diretorioExecucao, diretorioMaisRecente}: DiretoriosFotografia): Promise<void> {
    await fs.mkdir(diretorioExecucao, {recursive: true});
    await fs.mkdir(diretorioMaisRecente, {recursive: true});
}

async function persistirFotografia(
    fotografia: FotografiaColeta,
    {diretorioExecucao, diretorioMaisRecente}: DiretoriosFotografia
): Promise<string> {
    const caminhoFotografia = path.join(diretorioExecucao, NOME_ARQUIVO_FOTOGRAFIA);
    const conteudo = JSON.stringify(fotografia, null, 2);
    await fs.writeFile(caminhoFotografia, conteudo);
    await fs.writeFile(path.join(diretorioMaisRecente, NOME_ARQUIVO_FOTOGRAFIA), conteudo);
    return caminhoFotografia;
}

export {
    criarFotografiaColeta,
    persistirFotografia,
    prepararDiretoriosFotografia,
    type FotografiaColeta
};
