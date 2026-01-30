import {apiPost} from "@/utils/apiUtils";

export async function marcarComoLido(codAlerta: number): Promise<void> {
    await apiPost<void>(`/alertas/${codAlerta}/marcar-como-lido`);
}
