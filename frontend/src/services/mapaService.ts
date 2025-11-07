import apiClient from '../axios-setup';
import type {DisponibilizarMapaRequest, ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from '@/types/tipos';
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from '@/mappers/mapas';

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
  const response = await apiClient.post(`/subprocessos/${codSubprocesso}/mapa-completo/salvar`, data);
  return mapMapaCompletoDtoToModel(response.data);
};

export const obterMapaAjuste = async (id: number): Promise<MapaAjuste> => {
  const response = await apiClient.get(`/subprocessos/${id}/mapa-ajuste`);
  return mapMapaAjusteDtoToModel(response.data);
};

export const salvarMapaAjuste = async (codSubprocesso: number, data: any): Promise<void> => {
  await apiClient.post(`/subprocessos/${codSubprocesso}/mapa-ajuste/salvar`, data);
};

export async function verificarMapaVigente(codigoUnidade: number): Promise<boolean> {
  try {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/mapa-vigente`);
    return response.data.temMapaVigente;
  } catch (error: any) {
    if (error?.response?.status === 404) {
      return false;
    }
    throw error;
  }
}

export const disponibilizarMapa = async (codSubprocesso: number, data: DisponibilizarMapaRequest): Promise<void> => {
  await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar`, data);
};
