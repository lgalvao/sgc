import type {
    Processo,
    ProcessoDetalheResponseBackend,
    ProcessoResumo,
    UnidadeImportacao,
    UnidadeParticipanteDto
} from "./types";
import {mapearProcessoDetalhe, mapearUnidadeImportacao} from "./mapeadores";
import apiClient from "@/axios-setup";

const CAMINHO_PROCESSOS = "/processos";

function caminhoProcesso(codProcesso?: number, sufixo = ""): string {
    return codProcesso === undefined ? `${CAMINHO_PROCESSOS}${sufixo}` : `${CAMINHO_PROCESSOS}/${codProcesso}${sufixo}`;
}

export async function buscarProcessosFinalizados(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        caminhoProcesso(undefined, "/finalizados"),
    );
    return response.data;
}

export async function buscarProcessosParaImportacao(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        caminhoProcesso(undefined, "/finalizados?elegivelImportacao=true"),
    );
    return response.data;
}

export async function buscarUnidadesParaImportacao(codProcesso: number): Promise<UnidadeImportacao[]> {
    const response = await apiClient.get<UnidadeParticipanteDto[]>(
        caminhoProcesso(codProcesso, "/unidades-importacao"),
    );
    return response.data.map(mapearUnidadeImportacao);
}

export async function obterDetalhesProcesso(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/detalhes"));
    return mapearProcessoDetalhe(response.data);
}

export async function buscarContextoCompleto(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/contexto-completo"));
    return mapearProcessoDetalhe(response.data);
}
