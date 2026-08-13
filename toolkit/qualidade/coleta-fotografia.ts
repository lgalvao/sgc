import fs from "node:fs/promises";
import path from "node:path";
import {NOME_ARQUIVO_FOTOGRAFIA} from "../biblioteca/qualidade.js";
import {extrairPontosCriticosQualidade} from "./coleta-leitores.js";
import type {ExecucaoQualidade} from "./coleta-motor.js";

const VERSAO_SCHEMA_FOTOGRAFIA = "3.0.0" as const;

type MetadadosControleVersao = Readonly<Record<string, string>>;

interface FotografiaColeta {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    metadados: {
        geradoEm: string;
        perfilExecucao: string;
        duracaoTotalMs: number;
        controleVersao: MetadadosControleVersao;
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
    pontosCriticos: Array<{
        nome: string;
        risco: number;
        origem: string;
    }>;
}

interface OpcoesFotografia {
    versaoSchema: typeof VERSAO_SCHEMA_FOTOGRAFIA;
    perfilExecucao: string;
    inicio: number;
    verificacoes: ExecucaoQualidade[];
    controleVersao: MetadadosControleVersao;
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
    controleVersao,
    agora = new Date()
}: OpcoesFotografia): FotografiaColeta {
    const pontosCriticos = verificacoes
        .flatMap((item) => extrairPontosCriticosQualidade(item.metricas).map((pontoCritico) => ({
            nome: pontoCritico.arquivo,
            risco: pontoCritico.pontuacao,
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
            controleVersao
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
        pontosCriticos
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
    VERSAO_SCHEMA_FOTOGRAFIA,
    criarFotografiaColeta,
    persistirFotografia,
    prepararDiretoriosFotografia,
    type FotografiaColeta,
    type MetadadosControleVersao
};
