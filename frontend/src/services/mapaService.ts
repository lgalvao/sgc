import {AxiosError} from "axios";
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from "@/mappers/mapas";
import type {ImpactoMapa} from "@/types/impacto";
import type {DisponibilizarMapaRequest, MapaAjuste, MapaCompleto, MapaVisualizacao,} from "@/types/tipos";
import apiClient from "../axios-setup";

export async function obterMapaVisualizacao(
    codSubprocesso: number,
): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(
        `/subprocessos/${codSubprocesso}/mapa-visualizacao`,
    );
  return response.data;
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
  const response = await apiClient.get(`/subprocessos/${codSubprocesso}/impactos-mapa`);
  return mapImpactoMapaDtoToModel(response.data);
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
  const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-completo`);
  return mapMapaCompletoDtoToModel(response.data);
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: any,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-completo/salvar`,
        data,
    );
  return mapMapaCompletoDtoToModel(response.data);
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
  const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
  return mapMapaAjusteDtoToModel(response.data);
}

export async function salvarMapaAjuste(
    codSubprocesso: number,
    data: any,
): Promise<void> {
    await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-ajuste/salvar`,
        data,
    );
}

export async function verificarMapaVigente(
    codigoUnidade: number,
): Promise<boolean> {
  try {
      const response = await apiClient.get(
          `/unidades/${codigoUnidade}/mapa-vigente`,
      );
    return response.data.temMapaVigente;
  } catch (error) {
    if (error instanceof AxiosError && error.response?.status === 404) {
      return false;
    }
    throw error;
  }
}

export async function disponibilizarMapa(
    codSubprocesso: number,
    data: DisponibilizarMapaRequest,
): Promise<void> {
  await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-mapa`, data);
}
