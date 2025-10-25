import apiClient from '../axios-setup';
import type { Competencia, MapaCompleto } from '@/types/tipos';
import { mapMapaCompletoDtoToModel } from '@/mappers/mapas';

interface ImportarAtividadesRequest {
  subprocessoOrigemId: number;
}

export async function importarAtividades(idSubprocessoDestino: number, idSubprocessoOrigem: number): Promise<void> {
  const request: ImportarAtividadesRequest = { subprocessoOrigemId: idSubprocessoOrigem };
  try {
    await apiClient.post(`/subprocessos/${idSubprocessoDestino}/importar-atividades`, request);
  } catch (error) {
    console.error(`Erro ao importar atividades para o subprocesso ${idSubprocessoDestino}:`, error);
    throw error;
  }
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
    const response = await apiClient.put(`/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}`, competencia);
    return mapMapaCompletoDtoToModel(response.data);
}

export const removerCompetencia = async (codSubprocesso: number, idCompetencia: number): Promise<MapaCompleto> => {
    const response = await apiClient.delete(`/subprocessos/${codSubprocesso}/competencias/${idCompetencia}`);
    return mapMapaCompletoDtoToModel(response.data);
}
