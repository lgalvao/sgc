import apiClient from '../axios-setup';
import type { MapaVisualizacao, ImpactoMapa, MapaCompleto, MapaAjuste } from '@/types/tipos';
import {
    mapImpactoMapaDtoToModel,
    mapMapaCompletoDtoToModel,
    mapMapaAjusteDtoToModel,
} from '@/mappers/mapas';

interface ImportarAtividadesRequest {
  subprocessoOrigemId: number;
}

export async function importarAtividades(idSubprocessoDestino: number, idSubprocessoOrigem: number): Promise<void> {
  try {
    const request: ImportarAtividadesRequest = {
      subprocessoOrigemId: idSubprocessoOrigem,
    };
    await apiClient.post(`/subprocessos/${idSubprocessoDestino}/importar-atividades`, request);
  } catch (error) {
    console.error(`Erro ao importar atividades para o subprocesso ${idSubprocessoDestino}:`, error);
    throw error;
  }
}

export async function obterMapaVisualizacao(idSubprocesso: number): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(`/subprocessos/${idSubprocesso}/mapa-visualizacao`);
    return response.data;
}

export const verificarImpactosMapa = async (id: number): Promise<ImpactoMapa> => {
    const response = await apiClient.get(`/subprocessos/${id}/impactos-mapa`);
    return mapImpactoMapaDtoToModel(response.data);
};

export const obterMapaCompleto = async (id: number): Promise<MapaCompleto> => {
    const response = await apiClient.get(`/subprocessos/${id}/mapa-completo`);
    return mapMapaCompletoDtoToModel(response.data);
};

export const salvarMapaCompleto = async (id: number, data: any): Promise<MapaCompleto> => {
    const response = await apiClient.put(`/subprocessos/${id}/mapa-completo`, data);
    return mapMapaCompletoDtoToModel(response.data);
};

export const obterMapaAjuste = async (id: number): Promise<MapaAjuste> => {
    const response = await apiClient.get(`/subprocessos/${id}/mapa-ajuste`);
    return mapMapaAjusteDtoToModel(response.data);
};

export const salvarMapaAjuste = async (id: number, data: any): Promise<void> => {
    await apiClient.put(`/subprocessos/${id}/mapa-ajuste`, data);
};