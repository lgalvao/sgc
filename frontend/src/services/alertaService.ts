import apiClient from "../axios-setup";

export async function marcarComoLido(id: number): Promise<void> {
  await apiClient.post(`/alertas/${id}/marcar-como-lido`);
}
