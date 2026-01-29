import {
    mapAtividadeToModel,
    mapConhecimentoToModel,
    mapCriarAtividadeRequestToDto,
    mapAtualizarAtividadeToDto,
    mapCriarConhecimentoRequestToDto,
    mapAtualizarConhecimentoToDto,
} from "@/mappers/atividades";
import type {Atividade, AtividadeOperacaoResponse, Conhecimento, CriarConhecimentoRequest,} from "@/types/tipos";
import apiClient from "@/axios-setup";

export async function listarAtividades(): Promise<Atividade[]> {
    const response = await apiClient.get<any[]>("/atividades");
    return response.data.map(mapAtividadeToModel);
}

export async function obterAtividadePorCodigo(codAtividade: number): Promise<Atividade> {
    const response = await apiClient.get<any>(`/atividades/${codAtividade}`);
    return mapAtividadeToModel(response.data);
}

export async function criarAtividade(
    request: any,
    codMapa: number,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = mapCriarAtividadeRequestToDto(request, codMapa);
    const response = await apiClient.post<AtividadeOperacaoResponse>("/atividades", requestDto);
    return response.data;
}

export async function atualizarAtividade(
    codAtividade: number,
    request: Atividade,
): Promise<AtividadeOperacaoResponse> {
    const payload = mapAtualizarAtividadeToDto(request);
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/atualizar`,
        payload,
    );
    return response.data;
}

export async function excluirAtividade(codAtividade: number): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponse>(`/atividades/${codAtividade}/excluir`);
    return response.data;
}

export async function listarConhecimentos(
    codAtividade: number,
): Promise<Conhecimento[]> {
    const response = await apiClient.get<any[]>(
        `/atividades/${codAtividade}/conhecimentos`,
    );
    return response.data.map(mapConhecimentoToModel);
}

export async function criarConhecimento(
    codAtividade: number,
    request: CriarConhecimentoRequest,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = mapCriarConhecimentoRequestToDto(request, codAtividade);
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/conhecimentos`,
        requestDto,
    );
    return response.data;
}

export async function atualizarConhecimento(
    codAtividade: number,
    codConhecimento: number,
    request: Conhecimento,
): Promise<AtividadeOperacaoResponse> {
    const payload = mapAtualizarConhecimentoToDto(request, codAtividade);
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/atualizar`,
        payload,
    );
    return response.data;
}

export async function excluirConhecimento(
    codAtividade: number,
    codConhecimento: number,
): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/excluir`,
    );
    return response.data;
}
