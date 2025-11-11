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

export async function adicionarCompetencia(codSubprocesso: number, competencia: Competencia): Promise<MapaCompleto> {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias`, competencia);
    return mapMapaCompletoDtoToModel(response.data);
}

export async function atualizarCompetencia(codSubprocesso: number, competencia: Competencia): Promise<MapaCompleto> {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}/atualizar`, competencia);
    return mapMapaCompletoDtoToModel(response.data);
}

export async function removerCompetencia(codSubprocesso: number, idCompetencia: number): Promise<MapaCompleto> {
    const response = await apiClient.post(`/subprocessos/${codSubprocesso}/competencias/${idCompetencia}/remover`);
    return mapMapaCompletoDtoToModel(response.data);
}
