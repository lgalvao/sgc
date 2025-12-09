import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import {mapAtividadeVisualizacaoToModel} from "@/mappers/atividades";
import type {Competencia, MapaCompleto, Atividade, SubprocessoPermissoes, ValidacaoCadastro} from "@/types/tipos";
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

export async function listarAtividades(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<any[]>(`/subprocessos/${codSubprocesso}/atividades`);
    return response.data.map(mapAtividadeVisualizacaoToModel);
}

export async function obterPermissoes(codSubprocesso: number): Promise<SubprocessoPermissoes> {
    const response = await apiClient.get<SubprocessoPermissoes>(`/subprocessos/${codSubprocesso}/permissoes`);
    return response.data;
}

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> {
    const response = await apiClient.get<ValidacaoCadastro>(`/subprocessos/${codSubprocesso}/validar-cadastro`);
    return response.data;
}

export async function obterStatus(codSubprocesso: number): Promise<any> { // Using any properly typed with SubprocessoStatus from tipos.ts would be better but SubprocessoStatus IS in tipos.ts, let's import it.
    const response = await apiClient.get<any>(`/subprocessos/${codSubprocesso}/status`);
    return response.data;
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
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesAssociadas,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencias`,
        requestBody,
    );
    return mapMapaCompletoDtoToModel(response.data);
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    competencia: Competencia,
): Promise<MapaCompleto> {
    const requestBody = {
        descricao: competencia.descricao,
        atividadesIds: competencia.atividadesAssociadas,
    };
    const response = await apiClient.post(
        `/subprocessos/${codSubprocesso}/competencias/${competencia.codigo}/atualizar`,
        requestBody,
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
