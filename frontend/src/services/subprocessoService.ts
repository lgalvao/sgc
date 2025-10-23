import apiClient from '../axios-setup';
import type {Competencia, ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao} from '@/types/tipos';
import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from '@/mappers/mapas';

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

export async function fetchSubprocessoDetalhe(id: number, perfil: string, unidadeCodigo: number) {
  const response = await apiClient.get(`/subprocessos/${id}`, {
    params: {
      perfil,
      unidadeUsuario: unidadeCodigo,
    },
  });
  return response.data;
}

export async function disponibilizarCadastro(id: number) {
  return apiClient.post(`/subprocessos/${id}/disponibilizar`);
}

export async function disponibilizarRevisaoCadastro(id: number) {
  return apiClient.post(`/subprocessos/${id}/disponibilizar-revisao`);
}

export async function devolverCadastro(id: number, dados: { motivo: string; observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/devolver-cadastro`, dados);
}

export async function aceitarCadastro(id: number, dados: { observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/aceitar-cadastro`, dados);
}

export async function homologarCadastro(id: number, dados: { observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/homologar-cadastro`, dados);
}

export async function devolverRevisaoCadastro(id: number, dados: { motivo: string; observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/devolver-revisao-cadastro`, dados);
}

export async function aceitarRevisaoCadastro(id: number, dados: { observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/aceitar-revisao-cadastro`, dados);
}

export async function homologarRevisaoCadastro(id: number, dados: { observacoes: string }) {
  return apiClient.post(`/subprocessos/${id}/homologar-revisao-cadastro`, dados);
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
        return response.data.temMapaVigente; // Assumindo que o backend retorna um objeto com a propriedade 'temMapaVigente'
    } catch (error) {
        console.error(`Erro ao verificar mapa vigente para a unidade ${codigoUnidade}:`, error);
        return false; // Em caso de erro, assume que não há mapa vigente
    }
}