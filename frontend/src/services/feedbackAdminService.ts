import apiClient from "@/axios-setup";

export type FeedbackAdminTipo = "BUG" | "SUGESTAO" | "QUESTAO" | "ELOGIO";
export type FeedbackAdminStatus = "NOVO" | "REVISADO" | "RESOLVIDO" | "DESCARTADO";

export interface FeedbackAdmin {
    codigo: string;
    tipo: FeedbackAdminTipo;
    nota: string;
    metadataJson?: string | null;
    caminhoScreenshot?: string | null;
    screenshotDisponivel: boolean;
    usuarioCodigo: string;
    usuarioNome: string;
    enviadoEm: string;
    rota: string;
    status: FeedbackAdminStatus;
}

export async function listarFeedbacksAdmin(limite = 100): Promise<FeedbackAdmin[]> {
    const response = await apiClient.get<FeedbackAdmin[]>("/feedback/listar", {
        params: {limite}
    });
    return response.data;
}

export function obterUrlScreenshot(codigo: string): string {
    const baseUrl = apiClient.defaults.baseURL || "/api";
    const base = baseUrl.endsWith("/") ? baseUrl.slice(0, -1) : baseUrl;
    return `${base}/feedback/${codigo}/screenshot`;
}
