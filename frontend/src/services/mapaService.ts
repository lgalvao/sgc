import apiClient from '../axios-setup';
import type {DisponibilizarMapaRequest, MapaAjuste, MapaCompleto, MapaVisualizacao} from '@/types/tipos';
import type {ImpactoMapa} from '@/types/impacto';
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from '@/mappers/mapas';
import {AxiosError} from "axios";

export async function obterMapaVisualizacao(codSubrocesso: number): Promise<MapaVisualizacao> {
  const response = await apiClient.get<MapaVisualizacao>(`/subprocessos/${codSubrocesso}/mapa-visualizacao`);
  return response.data;
}

export async function verificarImpactosMapa(id: number): Promise<ImpactoMapa> {
  const response = await apiClient.get(`/subprocessos/${id}/impactos-mapa`);
  return mapImpactoMapaDtoToModel(response.data);
};

export async function obterMapaCompleto(id: number): Promise<MapaCompleto> {
  const response = await apiClient.get(`/subprocessos/${id}/mapa-completo`);
  return mapMapaCompletoDtoToModel(response.data);
};

export async function salvarMapaCompleto(codSubprocesso: number, data: any): Promise<MapaCompleto> {
  const response = await apiClient.post(`/subprocessos/${codSubprocesso}/mapa-completo/salvar`, data);
  return mapMapaCompletoDtoToModel(response.data);
};

export async function obterMapaAjuste(id: number): Promise<MapaAjuste> {
  const response = await apiClient.get(`/subprocessos/${id}/mapa-ajuste`);
  return mapMapaAjusteDtoToModel(response.data);
};

export async function salvarMapaAjuste(codSubprocesso: number, data: any): Promise<void> {
  await apiClient.post(`/subprocessos/${codSubprocesso}/mapa-ajuste/salvar`, data);
};

export async function verificarMapaVigente(codigoUnidade: number): Promise<boolean> {
  try {
    const response = await apiClient.get(`/unidades/${codigoUnidade}/mapa-vigente`);
    return response.data.temMapaVigente;
  } catch (error) {
    if (error instanceof AxiosError && error.response?.status === 404) {
      return false;
    }
    throw error;
  }
}

export async function disponibilizarMapa(codSubprocesso: number, data: DisponibilizarMapaRequest): Promise<void> {
  await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar`, data);
};
