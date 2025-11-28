import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import type {Competencia, MapaCompleto} from "@/types/tipos";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
  codSubprocessoOrigem: number;
}

export async function importarAtividades(
    codSubprocessoDestino: number,
    codSubprocessoOrigem: number,
): Promise<void> {
    const request: ImportarAtividadesRequest = {
        codSubprocessoOrigem: codSubprocessoOrigem,
    };
    await apiClient.post(
        `/subprocessos/${codSubprocessoDestino}/importar-atividades`,
        request,
    );
}

export async function buscarSubprocessoDetalhe(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
  const response = await apiClient.get(`/subprocessos/${codSubprocesso}`, {
    params: { perfil, unidadeUsuario: unidadeCodigo },
  });
  return response.data;
}

export async function buscarSubprocessoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
) {
    const response = await apiClient.get("/subprocessos/buscar", {
        params: {codProcesso, siglaUnidade},
    });
    return response.data;
}

export async function adicionarCompetencia(
    codSubprocesso: number,
    competencia: Competencia,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencias`,
        competencia,
    );
    return mapMapaCompletoDtoToModel(response.data);
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    competencia: Competencia,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}/atualizar`,
        competencia,
    );
    return mapMapaCompletoDtoToModel(response.data);
}

export async function removerCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
): Promise<MapaCompleto> {
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencias/${codCompetencia}/remover`,
    );
    return mapMapaCompletoDtoToModel(response.data);
}
