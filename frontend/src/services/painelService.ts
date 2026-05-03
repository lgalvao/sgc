import type {PainelBootstrap, ProcessoResumo} from "@/types/tipos";
import apiClient from "../axios-setup";

export async function obterBootstrap(): Promise<PainelBootstrap> {
    const response = await apiClient.get<PainelBootstrap>("/painel/bootstrap");
    return response.data;
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export interface ListarParams<T = string> {
    codUnidade?: number;
    page?: number;
    size?: number;
    sort?: T;
    order?: "asc" | "desc";
}

export async function listarProcessos(
    params: ListarParams<keyof ProcessoResumo> = {}
): Promise<Page<ProcessoResumo>> {
    const { codUnidade, page = 0, size = 20, sort, order } = params;
    const queryParams: Record<string, string | number> = {
        page,
        size,
    };
    if (codUnidade !== undefined && codUnidade !== null) {
        queryParams.unidade = codUnidade;
    }
    if (sort) {
        queryParams.sort = `${sort},${order}`;
    }
    const response = await apiClient.get<Page<ProcessoResumo>>("/painel/processos", {
        params: queryParams,
    });
    return response.data;
}

export async function marcarAlertasLidos(codigos: number[]): Promise<void> {
    await apiClient.post("/painel/alertas/marcar-lidos", codigos);
}
