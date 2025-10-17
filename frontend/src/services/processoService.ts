import apiClient from '../axios-setup';
import {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    mapProcessoDetalheDtoToFrontend,
    mapProcessoDtoToFrontend,
    Processo,
    ProcessoDetalhe,
} from '../mappers/processos';

export async function criarProcesso(request: CriarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.post<any>('/processos', request);
    return mapProcessoDtoToFrontend(response.data);
  } catch (error) {
    console.error('Erro ao criar processo:', error);
    throw error;
  }
}

export async function obterProcessoPorId(id: number): Promise<Processo> {
  try {
    const response = await apiClient.get<any>(`/processos/${id}`);
    return mapProcessoDtoToFrontend(response.data);
  } catch (error) {
    console.error(`Erro ao obter processo ${id}:`, error);
    throw error;
  }
}

export async function atualizarProcesso(id: number, request: AtualizarProcessoRequest): Promise<Processo> {
  try {
    const response = await apiClient.put<any>(`/processos/${id}`, request);
    return mapProcessoDtoToFrontend(response.data);
  } catch (error) {
    console.error(`Erro ao atualizar processo ${id}:`, error);
    throw error;
  }
}

export async function excluirProcesso(id: number): Promise<void> {
  try {
    await apiClient.delete(`/processos/${id}`);
  } catch (error) {
    console.error(`Erro ao excluir processo ${id}:`, error);
    throw error;
  }
}

export async function obterDetalhesProcesso(id: number): Promise<ProcessoDetalhe> {
  try {
    const response = await apiClient.get<any>(`/processos/${id}/detalhes`);
    return mapProcessoDetalheDtoToFrontend(response.data);
  } catch (error) {
    console.error(`Erro ao obter detalhes do processo ${id}:`, error);
    throw error;
  }
}