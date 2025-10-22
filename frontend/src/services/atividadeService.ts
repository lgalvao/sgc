import apiClient from '../axios-setup';
import {
    mapAtividadeDtoToModel,
    mapConhecimentoDtoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto
} from '@/mappers/atividades';
import type {Atividade, Conhecimento, CriarConhecimentoRequest} from '@/types/tipos';

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

export async function criarAtividade(request: any, idSubprocesso: number): Promise<Atividade> {
    try {
        const requestDto = mapCriarAtividadeRequestToDto(request, idSubprocesso);
        const response = await apiClient.post<any>('/atividades', requestDto);
        return mapAtividadeDtoToModel(response.data);
    } catch (error) {
        console.error('Erro ao criar atividade:', error);
        throw error;
    }
}

export async function atualizarAtividade(codAtividade: number, request: Atividade): Promise<Atividade> {
    try {
        // Para a atualização, podemos enviar o objeto completo
        const response = await apiClient.put<any>(`/atividades/${codAtividade}`, request);
        return mapAtividadeDtoToModel(response.data);
    } catch (error) {
        console.error(`Erro ao atualizar atividade ${codAtividade}:`, error);
        throw error;
    }
}

export async function excluirAtividade(codAtividade: number): Promise<void> {
  try {
    await apiClient.delete(`/atividades/${codAtividade}`);
  } catch (error) {
    console.error(`Erro ao excluir atividade ${codAtividade}:`, error);
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

export async function atualizarConhecimento(codAtividade: number, codConhecimento: number, request: Conhecimento): Promise<Conhecimento> {
    try {
        const response = await apiClient.put<any>(`/atividades/${codAtividade}/conhecimentos/${codConhecimento}`, request);
        return mapConhecimentoDtoToModel(response.data);
    } catch (error) {
        console.error(`Erro ao atualizar conhecimento ${codConhecimento}:`, error);
        throw error;
    }
}

export async function excluirConhecimento(codAtividade: number, codConhecimento: number): Promise<void> {
    try {
        await apiClient.delete(`/atividades/${codAtividade}/conhecimentos/${codConhecimento}`);
    } catch (error) {
        console.error(`Erro ao excluir conhecimento ${codConhecimento}:`, error);
        throw error;
    }
}