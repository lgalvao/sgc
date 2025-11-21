import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    Subprocesso,
    SubprocessoElegivel,
} from "@/types/tipos";
import apiClient from "../axios-setup";

export async function criarProcesso(
    request: CriarProcessoRequest,
): Promise<Processo> {
    const response = await apiClient.post<Processo>("/processos", request);
    return response.data;
}

export async function fetchProcessosFinalizados(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        "/processos/finalizados",
    );
    return response.data;
}

export async function iniciarProcesso(
    id: number,
    tipo: string,
    unidadesIds: number[],
): Promise<void> {
    await apiClient.post(`/processos/${id}/iniciar`, {
        tipo,
        unidades: unidadesIds
    });
}

export async function finalizarProcesso(id: number): Promise<void> {
    await apiClient.post(`/processos/${id}/finalizar`);
}

export async function obterProcessoPorId(id: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${id}`);
    return response.data;
}

export async function atualizarProcesso(
    codProcesso: number,
    request: AtualizarProcessoRequest,
): Promise<Processo> {
    const response = await apiClient.post<Processo>(
        `/processos/${codProcesso}/atualizar`,
        request,
    );
    return response.data;
}

export async function excluirProcesso(codProcesso: number): Promise<void> {
    await apiClient.post(`/processos/${codProcesso}/excluir`);
}

export async function obterDetalhesProcesso(id: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${id}/detalhes`);
    return response.data;
}

export async function processarAcaoEmBloco(payload: {
    codProcesso: number;
    unidades: string[];
    tipoAcao: "aceitar" | "homologar";
    unidadeUsuario: string;
}): Promise<void> {
    await apiClient.post(
        `/processos/${payload.codProcesso}/acoes-em-bloco`,
        payload,
    );
}

export async function fetchSubprocessosElegiveis(
    codProcesso: number,
): Promise<SubprocessoElegivel[]> {
    const response = await apiClient.get<SubprocessoElegivel[]>(
        `/processos/${codProcesso}/subprocessos-elegiveis`,
    );
    return response.data;
}

export async function alterarDataLimiteSubprocesso(
    id: number,
    dados: { novaData: string },
): Promise<void> {
    await apiClient.post(`/processos/alterar-data-limite`, {id, ...dados});
}

export async function apresentarSugestoes(
    id: number,
    dados: { sugestoes: string },
): Promise<void> {
    await apiClient.post(`/processos/apresentar-sugestoes`, {id, ...dados});
}

export async function validarMapa(id: number): Promise<void> {
    await apiClient.post(`/processos/validar-mapa`, {id});
}

export async function buscarSubprocessos(
    processoId: number,
): Promise<Subprocesso[]> {
    const response = await apiClient.get(`/processos/${processoId}/subprocessos`);
    return response.data;
}
