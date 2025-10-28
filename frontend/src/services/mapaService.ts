import apiClient from '../axios-setup';
import type { ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao } from '@/types/tipos';
import {
  mapImpactoMapaDtoToModel,
  mapMapaAjusteDtoToModel,
  mapMapaCompletoDtoToModel,
} from '@/mappers/mapas';

export async function obterMapaVisualizacao(codSubrocesso: number): Promise<MapaVisualizacao> {
  const response = await apiClient.get<MapaVisualizacao>(`/subprocessos/${codSubrocesso}/mapa-visualizacao`);
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

export const salvarMapaCompleto = async (codSubprocesso: number, data: any): Promise<MapaCompleto> => {
  const response = await apiClient.put(`/subprocessos/${codSubprocesso}/mapa-completo`, data);
  return mapMapaCompletoDtoToModel(response.data);
};

export const obterMapaAjuste = async (id: number): Promise<MapaAjuste> => {
  const response = await apiClient.get(`/subprocessos/${id}/mapa-ajuste`);
  return mapMapaAjusteDtoToModel(response.data);
};

export const salvarMapaAjuste = async (codSubprocesso: number, data: any): Promise<void> => {
  await apiClient.put(`/subprocessos/${codSubprocesso}/mapa-ajuste`, data);
};

export async function verificarMapaVigente(codigoUnidade: number): Promise<boolean> {
  try {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/mapa-vigente`);
    return response.data.temMapaVigente;
  } catch (error) {
    console.error(`Erro ao verificar mapa vigente para a unidade ${codigoUnidade}:`, error);
    return false;
  }
}
