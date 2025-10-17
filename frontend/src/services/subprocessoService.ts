import apiClient from '../axios-setup';

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