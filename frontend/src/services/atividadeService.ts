import apiClient from '../axios-setup';
import {
  mapAtividadeDtoToModel,
  mapConhecimentoDtoToModel,
  mapCriarAtividadeRequestToDto,
  mapCriarConhecimentoRequestToDto
} from '@/mappers/atividades';
import type { Atividade, Conhecimento } from '@/types/tipos';

export async function listarAtividades(): Promise<Atividade[]> {
  try {
    const response = await apiClient.get<any[]>('/atividades');
    return response.data.map(mapAtividadeDtoToModel);
  } catch (error) {
    console.error('Erro ao listar atividades:', error);
    throw error;
  }
}

export async function obterAtividadePorId(id: number): Promise<Atividade> {
  try {
    const response = await apiClient.get<any>(`/atividades/${id}`);
    return mapAtividadeDtoToModel(response.data);
  } catch (error) {
    console.error(`Erro ao obter atividade ${id}:`, error);
    throw error;
  }
}

export async function criarAtividade(request: CriarAtividadeRequest): Promise<Atividade> {
  try {
    const requestDto = mapCriarAtividadeRequestToDto(request);
    const response = await apiClient.post<any>('/atividades', requestDto);
    return mapAtividadeDtoToModel(response.data);
  } catch (error) {
    console.error('Erro ao criar atividade:', error);
    throw error;
  }
}

export async function atualizarAtividade(id: number, request: Atividade): Promise<Atividade> {
    try {
        // Para a atualização, podemos enviar o objeto completo
        const response = await apiClient.put<any>(`/atividades/${id}`, request);
        return mapAtividadeDtoToModel(response.data);
    } catch (error) {
        console.error(`Erro ao atualizar atividade ${id}:`, error);
        throw error;
    }
}

export async function excluirAtividade(id: number): Promise<void> {
  try {
    await apiClient.delete(`/atividades/${id}`);
  } catch (error) {
    console.error(`Erro ao excluir atividade ${id}:`, error);
    throw error;
  }
}

export async function listarConhecimentos(atividadeId: number): Promise<Conhecimento[]> {
    try {
        const response = await apiClient.get<any[]>(`/atividades/${atividadeId}/conhecimentos`);
        return response.data.map(mapConhecimentoDtoToModel);
    } catch (error) {
        console.error(`Erro ao listar conhecimentos para a atividade ${atividadeId}:`, error);
        throw error;
    }
}

export async function criarConhecimento(atividadeId: number, request: CriarConhecimentoRequest): Promise<Conhecimento> {
    try {
        const requestDto = mapCriarConhecimentoRequestToDto(request);
        const response = await apiClient.post<any>(`/atividades/${atividadeId}/conhecimentos`, requestDto);
        return mapConhecimentoDtoToModel(response.data);
    } catch (error) {
        console.error(`Erro ao criar conhecimento para a atividade ${atividadeId}:`, error);
        throw error;
    }
}

export async function atualizarConhecimento(atividadeId: number, conhecimentoId: number, request: Conhecimento): Promise<Conhecimento> {
    try {
        const response = await apiClient.put<any>(`/atividades/${atividadeId}/conhecimentos/${conhecimentoId}`, request);
        return mapConhecimentoDtoToModel(response.data);
    } catch (error) {
        console.error(`Erro ao atualizar conhecimento ${conhecimentoId}:`, error);
        throw error;
    }
}

export async function excluirConhecimento(atividadeId: number, conhecimentoId: number): Promise<void> {
    try {
        await apiClient.delete(`/atividades/${atividadeId}/conhecimentos/${conhecimentoId}`);
    } catch (error) {
        console.error(`Erro ao excluir conhecimento ${conhecimentoId}:`, error);
        throw error;
    }
}