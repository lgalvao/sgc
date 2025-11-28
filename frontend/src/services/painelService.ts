import {mapAlertaDtoToFrontend} from "@/mappers/alertas";
import {mapProcessoResumoDtoToFrontend} from "@/mappers/processos";
import type {Alerta, ProcessoResumo} from "@/types/tipos";
import apiClient from "../axios-setup";

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    number: number; // current page number
    size: number; // page size
    first: boolean;
    last: boolean;
    empty: boolean;
}

export async function listarProcessos(
    perfil: string,
    codUnidade?: number,
    page: number = 0,
    size: number = 20,
    sort?: keyof ProcessoResumo,
    order?: "asc" | "desc",
): Promise<Page<ProcessoResumo>> {
    const params: any = {
        perfil,
        page,
        size,
    };
    if (codUnidade !== undefined && codUnidade !== null) {
        params.unidade = codUnidade;
    }
    if (sort) {
        params.sort = `${sort},${order}`;
    }
    const response = await apiClient.get<Page<any>>("/painel/processos", {
        params,
    });
    return {
        ...response.data,
        content: response.data.content.map(mapProcessoResumoDtoToFrontend),
    };
}

export async function listarAlertas(
    usuarioTitulo?: number,
    codUnidade?: number,
    page: number = 0,
    size: number = 20,
    sort?: "data" | "processo",
    order?: "asc" | "desc",
): Promise<Page<Alerta>> {
    const params: any = {
        page,
        size,
    };
    if (usuarioTitulo !== undefined && usuarioTitulo !== null) {
        params.usuarioTitulo = usuarioTitulo;
    }
    if (codUnidade !== undefined && codUnidade !== null) {
        params.unidade = codUnidade;
    }
    if (sort) {
        params.sort = `${sort},${order}`;
    }
    const response = await apiClient.get<Page<any>>("/painel/alertas", {
        params,
    });
    return {
        ...response.data,
        content: response.data.content.map(mapAlertaDtoToFrontend),
    };
}
