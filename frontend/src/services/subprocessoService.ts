import apiClient from '../axios-setup';
import type { MapaVisualizacao } from '@/types/tipos';

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
    try {
        const response = await apiClient.get<MapaVisualizacao>(`/subprocessos/${idSubprocesso}/mapa-visualizacao`);
        return response.data;
    } catch (error) {
        console.error(`Erro ao obter mapa de visualização para o subprocesso ${idSubprocesso}:`, error);
        throw error;
    }
}