import {
    mapAtividadeDtoToModel,
    mapConhecimentoDtoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto,
} from "@/mappers/atividades";
import type { Atividade, Conhecimento, CriarConhecimentoRequest, } from "@/types/tipos";
import apiClient from "../axios-setup";

export async function listarAtividades(): Promise<Atividade[]> {
    const response = await apiClient.get<any[]>("/atividades");
    return response.data.map(mapAtividadeDtoToModel);
}

export async function obterAtividadePorCodigo(codAtividade: number): Promise<Atividade> {
    const response = await apiClient.get<any>(`/atividades/${codAtividade}`);
    return mapAtividadeDtoToModel(response.data);
}

export async function criarAtividade(
    request: any,
    codSubrocesso: number,
): Promise<Atividade> {
    const requestDto = mapCriarAtividadeRequestToDto(request, codSubrocesso);
    const response = await apiClient.post<any>("/atividades", requestDto);
    return mapAtividadeDtoToModel(response.data);
}

export async function atualizarAtividade(
    codAtividade: number,
    request: Atividade,
): Promise<Atividade> {
    const response = await apiClient.post<any>(
        `/atividades/${codAtividade}/atualizar`,
        request,
    );
    return mapAtividadeDtoToModel(response.data);
}

export async function excluirAtividade(codAtividade: number): Promise<void> {
    await apiClient.post(`/atividades/${codAtividade}/excluir`);
}

export async function listarConhecimentos(
    codAtividade: number,
): Promise<Conhecimento[]> {
    const response = await apiClient.get<any[]>(
        `/atividades/${codAtividade}/conhecimentos`,
    );
    return response.data.map(mapConhecimentoDtoToModel);
}

export async function criarConhecimento(
    codAtividade: number,
    request: CriarConhecimentoRequest,
): Promise<Conhecimento> {
    const requestDto = mapCriarConhecimentoRequestToDto(request, codAtividade);
    const response = await apiClient.post<any>(
        `/atividades/${codAtividade}/conhecimentos`,
        requestDto,
    );
    return mapConhecimentoDtoToModel(response.data);
}

export async function atualizarConhecimento(
    codAtividade: number,
    codConhecimento: number,
    request: Conhecimento,
): Promise<Conhecimento> {
    const payload = {
        codigo: request.id,
        atividadeCodigo: codAtividade,
        descricao: request.descricao,
    };
    const response = await apiClient.post<any>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/atualizar`,
        payload,
    );
    return mapConhecimentoDtoToModel(response.data);
}

export async function excluirConhecimento(
    codAtividade: number,
    codConhecimento: number,
): Promise<void> {
    await apiClient.post(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/excluir`,
    );
}
