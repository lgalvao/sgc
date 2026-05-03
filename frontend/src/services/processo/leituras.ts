import type {Processo, ProcessoResumo, UnidadeImportacao, ProcessoDetalheResponseBackend} from "./types";
import {mapearProcessoDetalhe} from "./mapeadores";
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
        caminhoProcesso(undefined, "/para-importacao"),
    );
    return response.data;
}

export async function buscarUnidadesParaImportacao(codProcesso: number): Promise<UnidadeImportacao[]> {
    const response = await apiClient.get<any[]>(
        caminhoProcesso(codProcesso, "/unidades-importacao"),
    );
    return response.data.map((dto) => ({
        nome: dto.nome!,
        sigla: dto.sigla!,
        codUnidade: dto.codUnidade,
        codSubprocesso: dto.codSubprocesso ?? 0,
        codUnidadeSuperior: dto.codUnidadeSuperior,
        situacaoSubprocesso: dto.situacaoSubprocesso,
        dataLimite: dto.dataLimite,
        mapaCodigo: dto.mapaCodigo,
        localizacaoAtualCodigo: dto.localizacaoAtualCodigo,
    }));
}

export async function obterDetalhesProcesso(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/detalhes"));
    return mapearProcessoDetalhe(response.data);
}

export async function buscarContextoCompleto(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/contexto-completo"));
    return mapearProcessoDetalhe(response.data);
}
