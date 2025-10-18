import apiClient from "@/axios-setup";
import type { Analise } from "@/types/types";

export const fetchAnalises = async (subprocessoId: number): Promise<Analise[]> => {
    const response = await apiClient.get(`/subprocessos/${subprocessoId}/analises`);
    return response.data;
};

export const aprovarAnalise = async (analiseId: number): Promise<void> => {
    await apiClient.post(`/analises/${analiseId}/aprovar`);
};

export const reprovarAnalise = async (analiseId: number, observacao: string): Promise<void> => {
    await apiClient.post(`/analises/${analiseId}/reprovar`, { observacao });
};