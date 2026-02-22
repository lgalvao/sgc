import {mapImpactoMapaDtoToModel, mapMapaAjusteDtoToModel, mapMapaCompletoDtoToModel,} from "@/mappers/mapas";
import type {DisponibilizarMapaRequest, ImpactoMapa, MapaAjuste, MapaCompleto, MapaVisualizacao,} from "@/types/tipos";
import {getOrNull} from "@/utils/apiError";
import apiClient from "../axios-setup";

export async function obterMapaVisualizacao(
    codSubprocesso: number,
): Promise<MapaVisualizacao> {
    const response = await apiClient.get<MapaVisualizacao>(
        `/subprocessos/${codSubprocesso}/mapa-visualizacao`,
    );
    return response.data;
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/impactos-mapa`);
    return mapImpactoMapaDtoToModel(response.data);
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-completo`);
    return mapMapaCompletoDtoToModel(response.data);
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: any,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-completo`,
        data,
    );
    return mapMapaCompletoDtoToModel(response.data);
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/mapa-ajuste`);
    return mapMapaAjusteDtoToModel(response.data);
}

export async function salvarMapaAjuste(
    codSubprocesso: number,
    data: any,
): Promise<void> {
    await apiClient.post(
        `/subprocessos/${codSubprocesso}/mapa-ajuste/atualizar`,
        data,
    );
}

export async function verificarMapaVigente(
    codigoUnidade: number,
): Promise<boolean> {
    const result = await getOrNull(async () => {
        const response = await apiClient.get(
            `/unidades/${codigoUnidade}/mapa-vigente`,
        );
        return response.data.temMapaVigente;
    });
    return result ?? false;
}

export async function disponibilizarMapa(
    codSubprocesso: number,
    data: DisponibilizarMapaRequest,
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-mapa`, data);
}
