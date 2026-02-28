import type {
    Atividade,
    AtividadeOperacaoResponse,
    Conhecimento,
    CriarAtividadeRequest,
    CriarConhecimentoRequest,
} from "@/types/tipos";
import apiClient from "@/axios-setup";

export async function listarAtividades(): Promise<Atividade[]> {
    const response = await apiClient.get<Atividade[]>("/atividades");
    return response.data;
}

export async function obterAtividadePorCodigo(codAtividade: number): Promise<Atividade> {
    const response = await apiClient.get<Atividade>(`/atividades/${codAtividade}`);
    return response.data;
}

export async function criarAtividade(
    request: CriarAtividadeRequest,
    codMapa: number,
): Promise<AtividadeOperacaoResponse> {
    const payload = {...request, mapaCodigo: codMapa};
    const response = await apiClient.post<AtividadeOperacaoResponse>("/atividades", payload);
    return response.data;
}

export async function atualizarAtividade(
    codAtividade: number,
    request: Atividade,
): Promise<AtividadeOperacaoResponse> {
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/atualizar`,
        request,
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
    const response = await apiClient.get<Conhecimento[]>(
        `/atividades/${codAtividade}/conhecimentos`,
    );
    return response.data;
}

export async function criarConhecimento(
    codAtividade: number,
    request: CriarConhecimentoRequest,
): Promise<AtividadeOperacaoResponse> {
    const payload = {...request, atividadeCodigo: codAtividade};
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        `/atividades/${codAtividade}/conhecimentos`,
        payload,
    );
    return response.data;
}

export async function atualizarConhecimento(
    codAtividade: number,
    codConhecimento: number,
    request: Conhecimento,
): Promise<AtividadeOperacaoResponse> {
    const payload = {descricao: request.descricao};
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
