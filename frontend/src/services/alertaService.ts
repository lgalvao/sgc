import apiClient from '../axios-setup';

export async function marcarComoLido(id: number): Promise<void> {
  try {
    await apiClient.post(`/alertas/${id}/marcar-como-lido`);
  } catch (error) {
    console.error(`Erro ao marcar alerta ${id} como lido:`, error);
    throw error;
  }
}