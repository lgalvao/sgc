import apiClient from "../axios-setup";

export async function marcarComoLido(codAlerta: number): Promise<void> {
    await apiClient.post(`/alertas/${codAlerta}/marcar-como-lido`);
}
