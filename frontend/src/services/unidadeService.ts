import {apiGet} from "@/utils/apiUtils";
import type {Unidade} from "@/types/tipos";

export async function buscarTodasUnidades(): Promise<Unidade[]> {
    const data = await apiGet("/unidades") as any[];
    return data.map(mapUnidade);
}

export async function buscarUnidadePorSigla(sigla: string): Promise<Unidade> {
    const data = await apiGet(`/unidades/sigla/${sigla}`);
    return mapUnidade(data);
}

export async function buscarUnidadePorCodigo(codigo: number): Promise<Unidade> {
    const data = await apiGet(`/unidades/${codigo}`);
    return mapUnidade(data);
}

export async function buscarArvoreComElegibilidade(
    tipoProcesso: string,
    codProcesso?: number,
): Promise<Unidade[]> {
    let url = `/unidades/arvore-com-elegibilidade?tipoProcesso=${tipoProcesso}`;
    if (codProcesso) {
        url += `&codProcesso=${codProcesso}`;
    }
    const data = await apiGet(url) as any[];
    return data.map(mapUnidade);
}

export async function buscarArvoreUnidade(codigo: number): Promise<Unidade> {
    const data = await apiGet(`/unidades/${codigo}/arvore`);
    return mapUnidade(data);
}

export async function buscarSubordinadas(sigla: string): Promise<string[]> {
    return apiGet(`/unidades/sigla/${sigla}/subordinadas`);
}

export async function buscarSuperior(sigla: string): Promise<string | null> {
    return apiGet(`/unidades/sigla/${sigla}/superior`) || null;
}

function mapUnidade(dto: any): Unidade {
    if (!dto) return dto;
    return {
        ...dto,
        filhas: (dto.subunidades || dto.filhas || []).map(mapUnidade)
    };
}
