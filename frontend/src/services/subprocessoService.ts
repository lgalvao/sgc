import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import {mapAtividadeVisualizacaoToModel} from "@/mappers/atividades";
import type {Atividade, Competencia, MapaCompleto, SubprocessoPermissoes, ValidacaoCadastro} from "@/types/tipos";
import type {AtividadeDto} from "@/types/dtos";
import apiClient from "../axios-setup";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
}

interface ProcessarEmBlocoRequest {
    unidadeCodigos: number[];
    dataLimite?: string;
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
    const response = await apiClient.get<AtividadeDto[]>(`/subprocessos/${codSubprocesso}/atividades`);
    return response.data.map(mapAtividadeVisualizacaoToModel).filter((a): a is Atividade => a !== null);
}

export async function obterPermissoes(codSubprocesso: number): Promise<SubprocessoPermissoes> {
    const response = await apiClient.get<SubprocessoPermissoes>(`/subprocessos/${codSubprocesso}/permissoes`);
    return response.data;
}

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> {
    const response = await apiClient.get<ValidacaoCadastro>(`/subprocessos/${codSubprocesso}/validar-cadastro`);
    return response.data;
}

export async function obterStatus(codSubprocesso: number): Promise<any> {
    const response = await apiClient.get<any>(`/subprocessos/${codSubprocesso}/status`);
    return response.data;
}

export async function buscarSubprocessoDetalhe(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
    });
    return response.data;
}

export async function buscarContextoEdicao(
    codSubprocesso: number,
    perfil: string,
    unidadeCodigo: number,
) {
    const response = await apiClient.get(`/subprocessos/${codSubprocesso}/contexto-edicao`, {
        params: {perfil, unidadeUsuario: unidadeCodigo},
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

// Métodos para ações em bloco

export async function aceitarCadastroEmBloco(
    codSubprocesso: number,
    payload: ProcessarEmBlocoRequest
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-cadastro-bloco`, payload);
}

export async function homologarCadastroEmBloco(
    codSubprocesso: number,
    payload: ProcessarEmBlocoRequest
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-cadastro-bloco`, payload);
}

export async function aceitarValidacaoEmBloco(
    codSubprocesso: number,
    payload: ProcessarEmBlocoRequest
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-validacao-bloco`, payload);
}

export async function homologarValidacaoEmBloco(
    codSubprocesso: number,
    payload: ProcessarEmBlocoRequest
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-validacao-bloco`, payload);
}

export async function disponibilizarMapaEmBloco(
    codSubprocesso: number,
    payload: ProcessarEmBlocoRequest
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/disponibilizar-mapa-bloco`, payload);
}
