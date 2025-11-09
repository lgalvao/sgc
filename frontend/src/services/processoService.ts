import apiClient from '../axios-setup';
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoDetalhe,
    ProcessoResumo,
    SubprocessoElegivel,
} from '@/types/tipos';
import {ApiError} from './ApiError';
import axios from 'axios';

async function handleError(error: unknown, context: string): Promise<never> {
    if (axios.isAxiosError(error)) {
        const { response } = error;
        const message = response?.data?.message || `Erro em ${context}`;
        const statusCode = response?.status || 500;
        throw new ApiError(message, statusCode, response?.data);
    }
    throw new Error(`Erro desconhecido em ${context}`);
}

export async function criarProcesso(request: CriarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.post<Processo>('/processos', request);
    return response.data;
  } catch (error) {
    return handleError(error, 'criar processo');
  }
}

export async function fetchProcessosFinalizados(): Promise<ProcessoResumo[]> {
  try {
    const response = await apiClient.get<ProcessoResumo[]>('/processos/finalizados');
    return response.data;
  } catch (error) {
    return handleError(error, 'buscar processos finalizados');
  }
}

export async function iniciarProcesso(id: number, tipo: string, unidadesIds: number[]): Promise<void> {
  try {
    await apiClient.post(`/processos/${id}/iniciar?tipo=${tipo}`, unidadesIds);
  } catch (error) {
    return handleError(error, `iniciar o processo ${id}`);
  }
}

export async function finalizarProcesso(id: number): Promise<void> {
  try {
    await apiClient.post(`/processos/${id}/finalizar`);
  } catch (error) {
    return handleError(error, `finalizar o processo ${id}`);
  }
}

export async function obterProcessoPorId(id: number): Promise<Processo> {
  try {
    const response = await apiClient.get<Processo>(`/processos/${id}`);
    return response.data;
  } catch (error) {
    return handleError(error, `obter processo ${id}`);
  }
}

export async function atualizarProcesso(codProcesso: number, request: AtualizarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.post<Processo>(`/processos/${codProcesso}/atualizar`, request);
    return response.data;
  } catch (error) {
    return handleError(error, `atualizar processo ${codProcesso}`);
  }
}

export async function excluirProcesso(codProcesso: number): Promise<void> {
  try {
    await apiClient.post(`/processos/${codProcesso}/excluir`);
  } catch (error) {
    return handleError(error, `excluir processo ${codProcesso}`);
  }
}

export async function obterDetalhesProcesso(id: number): Promise<ProcessoDetalhe> {
  try {
    const response = await apiClient.get<ProcessoDetalhe>(`/processos/${id}/detalhes`);
    return response.data;
  } catch (error) {
    return handleError(error, `obter detalhes do processo ${id}`);
  }
}

export async function processarAcaoEmBloco(payload: {
    codProcesso: number,
    unidades: string[],
    tipoAcao: 'aceitar' | 'homologar',
    unidadeUsuario: string
}): Promise<void> {
    try {
        await apiClient.post(`/processos/${payload.codProcesso}/acoes-em-bloco`, payload);
    } catch (error) {
        return handleError(error, `processar ação em bloco para o processo ${payload.codProcesso}`);
    }
}

export async function fetchSubprocessosElegiveis(codProcesso: number): Promise<SubprocessoElegivel[]> {
    try {
        const response = await apiClient.get<SubprocessoElegivel[]>(`/processos/${codProcesso}/subprocessos-elegiveis`);
        return response.data;
    } catch (error) {
        return handleError(error, `buscar subprocessos elegíveis para o processo ${codProcesso}`);
    }
}

export async function alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }): Promise<void> {
    try {
        await apiClient.post(`/processos/alterar-data-limite`, { id, ...dados });
    } catch (error) {
        return handleError(error, `alterar a data limite para o subprocesso ${id}`);
    }
}

export async function apresentarSugestoes(id: number, dados: { sugestoes: string }): Promise<void> {
    try {
        await apiClient.post(`/processos/apresentar-sugestoes`, { id, ...dados });
    } catch (error) {
        return handleError(error, `apresentar sugestões para o subprocesso ${id}`);
    }
}

export async function validarMapa(id: number): Promise<void> {
    try {
        await apiClient.post(`/processos/validar-mapa`, { id });
    } catch (error) {
        return handleError(error, `validar o mapa para o subprocesso ${id}`);
    }
}
