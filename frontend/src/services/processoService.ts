import apiClient from '../axios-setup';
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoDetalhe,
    ProcessoResumo,
} from '@/types/tipos';

export async function criarProcesso(request: CriarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.post<Processo>('/processos', request);
    return response.data;
  } catch (error) {
    console.error('Erro ao criar processo:', error);
    throw error;
  }
}

export async function fetchProcessosFinalizados(): Promise<ProcessoResumo[]> {
  try {
    const response = await apiClient.get<ProcessoResumo[]>('/processos/finalizados');
    return response.data;
  } catch (error) {
    console.error('Erro ao buscar processos finalizados:', error);
    throw error;
  }
}

export async function iniciarProcesso(id: number, tipo: string, unidadesIds: number[]): Promise<void> {
  try {
    await apiClient.post(`/processos/${id}/iniciar?tipo=${tipo}`, unidadesIds);
  } catch (error) {
    console.error(`Erro ao iniciar o processo ${id}:`, error);
    throw error;
  }
}

export async function finalizarProcesso(id: number): Promise<void> {
  try {
    await apiClient.post(`/processos/${id}/finalizar`);
  } catch (error) {
    console.error(`Erro ao finalizar o processo ${id}:`, error);
    throw error;
  }
}

export async function obterProcessoPorId(id: number): Promise<Processo> {
  try {
    const response = await apiClient.get<Processo>(`/processos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Erro ao obter processo ${id}:`, error);
    throw error;
  }
}

export async function atualizarProcesso(codProcesso: number, request: AtualizarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.put<Processo>(`/processos/${codProcesso}`, request);
    return response.data;
  } catch (error) {
    console.error(`Erro ao atualizar processo ${codProcesso}:`, error);
    throw error;
  }
}

export async function excluirProcesso(codProcesso: number): Promise<void> {
  try {
    await apiClient.delete(`/processos/${codProcesso}`);
  } catch (error) {
    console.error(`Erro ao excluir processo ${codProcesso}:`, error);
    throw error;
  }
}

export async function obterDetalhesProcesso(id: number): Promise<ProcessoDetalhe> {
  try {
    const response = await apiClient.get<ProcessoDetalhe>(`/processos/${id}/detalhes`);
    return response.data;
  } catch (error) {
    console.error(`Erro ao obter detalhes do processo ${id}:`, error);
    throw error;
  }
}

export async function processarAcaoEmBloco(payload: {
    idProcesso: number,
    unidades: string[],
    tipoAcao: 'aceitar' | 'homologar',
    unidadeUsuario: string
}): Promise<void> {
    try {
        await apiClient.post(`/processos/${payload.idProcesso}/acoes-em-bloco`, payload);
    } catch (error) {
        console.error(`Erro ao processar ação em bloco para o processo ${payload.idProcesso}:`, error);
        throw error;
    }
}