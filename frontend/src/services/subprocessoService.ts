import apiClient from '../axios-setup';
import type {Competencia, MapaCompleto} from '@/types/tipos';
import {mapMapaCompletoDtoToModel} from '@/mappers/mapas';

interface ImportarAtividadesRequest {
  subprocessoOrigemId: number;
}

export async function importarAtividades(codSubrocessoDestino: number, codSubrocessoOrigem: number): Promise<void> {
  const request: ImportarAtividadesRequest = { subprocessoOrigemId: codSubrocessoOrigem };
  await apiClient.post(`/subprocessos/${codSubrocessoDestino}/importar-atividades`, request);
}

export async function fetchSubprocessoDetalhe(id: number, perfil: string, unidadeCodigo: number) {
  const response = await apiClient.get(`/subprocessos/${id}`, {
    params: { perfil, unidadeUsuario: unidadeCodigo },
  });
  return response.data;
}

// Funções de competência mantidas aqui, pois estão fortemente acopladas ao subprocesso
export const adicionarCompetencia = async (codSubprocesso: number, competencia: Competencia): Promise<MapaCompleto> => {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias`, competencia);
    return mapMapaCompletoDtoToModel(response.data);
}

export const atualizarCompetencia = async (codSubprocesso: number, competencia: Competencia): Promise<MapaCompleto> => {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}/atualizar`, competencia);
    return mapMapaCompletoDtoToModel(response.data);
}

export const removerCompetencia = async (codSubprocesso: number, idCompetencia: number): Promise<MapaCompleto> => {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias/${idCompetencia}/remover`);
    return mapMapaCompletoDtoToModel(response.data);
}
