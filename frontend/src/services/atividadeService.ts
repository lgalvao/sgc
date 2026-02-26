import {
    mapAtividadeOperacaoResponseToModel,
    mapAtividadeToModel,
    mapAtualizarAtividadeToDto,
    mapAtualizarConhecimentoToDto,
    mapConhecimentoToModel,
    mapCriarAtividadeRequestToDto,
    mapCriarConhecimentoRequestToDto,
} from "@/services/subprocessoService";
import type {Atividade, AtividadeOperacaoResponse, Conhecimento, CriarAtividadeRequest, CriarConhecimentoRequest,} from "@/types/tipos";
import type {AtividadeDto, AtividadeOperacaoResponseDto, ConhecimentoDto} from "@/types/dtos";
import apiClient from "@/axios-setup";

export async function listarAtividades(): Promise<Atividade[]> {
    const response = await apiClient.get<AtividadeDto[]>("/atividades");
    return response.data.map(mapAtividadeToModel).filter((a): a is Atividade => a !== null);
}

export async function obterAtividadePorCodigo(codAtividade: number): Promise<Atividade> {
    const response = await apiClient.get<AtividadeDto>(`/atividades/${codAtividade}`);
    const model = mapAtividadeToModel(response.data);
    if (!model) throw new Error("Atividade não encontrada");
    return model;
}

export async function criarAtividade(
    request: CriarAtividadeRequest,
    codMapa: number,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = mapCriarAtividadeRequestToDto(request, codMapa);
    const response = await apiClient.post<AtividadeOperacaoResponseDto>("/atividades", requestDto);
    return mapAtividadeOperacaoResponseToModel(response.data);
}

export async function atualizarAtividade(
    codAtividade: number,
    request: Atividade,
): Promise<AtividadeOperacaoResponse> {
    const payload = mapAtualizarAtividadeToDto(request);
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/atualizar`,
        payload,
    );
    return mapAtividadeOperacaoResponseToModel(response.data);
}

export async function excluirAtividade(codAtividade: number): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(`/atividades/${codAtividade}/excluir`);
    return mapAtividadeOperacaoResponseToModel(response.data);
}

export async function listarConhecimentos(
    codAtividade: number,
): Promise<Conhecimento[]> {
    const response = await apiClient.get<ConhecimentoDto[]>(
        `/atividades/${codAtividade}/conhecimentos`,
    );
    return response.data.map(mapConhecimentoToModel).filter((c): c is Conhecimento => c !== null);
}

export async function criarConhecimento(
    codAtividade: number,
    request: CriarConhecimentoRequest,
): Promise<AtividadeOperacaoResponse> {
    const requestDto = mapCriarConhecimentoRequestToDto(request, codAtividade);
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos`,
        requestDto,
    );
    return mapAtividadeOperacaoResponseToModel(response.data);
}

export async function atualizarConhecimento(
    codAtividade: number,
    codConhecimento: number,
    request: Conhecimento,
): Promise<AtividadeOperacaoResponse> {
    const payload = mapAtualizarConhecimentoToDto(request, codAtividade);
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/atualizar`,
        payload,
    );
    return mapAtividadeOperacaoResponseToModel(response.data);
}

export async function excluirConhecimento(
    codAtividade: number,
    codConhecimento: number,
): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponseDto>(
        `/atividades/${codAtividade}/conhecimentos/${codConhecimento}/excluir`,
    );
    return mapAtividadeOperacaoResponseToModel(response.data);
}
