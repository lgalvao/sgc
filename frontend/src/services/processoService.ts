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

export async function buscarProcessosFinalizados(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        "/processos/finalizados",
    );
    return response.data;
}

export async function iniciarProcesso(
    codProcesso: number,
    tipo: string,
    codigosUnidades: number[],
): Promise<void> {
    await apiClient.post(`/processos/${codProcesso}/iniciar`, {
        tipo,
        unidades: codigosUnidades
    });
}

export async function finalizarProcesso(codProcesso: number): Promise<void> {
    await apiClient.post(`/processos/${codProcesso}/finalizar`);
}

export async function obterProcessoPorCodigo(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${codProcesso}`);
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

export async function obterDetalhesProcesso(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(`/processos/${codProcesso}/detalhes`);
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

export async function buscarSubprocessosElegiveis(
    codProcesso: number,
): Promise<SubprocessoElegivel[]> {
    const response = await apiClient.get<SubprocessoElegivel[]>(
        `/processos/${codProcesso}/subprocessos-elegiveis`,
    );
    return response.data;
}

export async function alterarDataLimiteSubprocesso(
    codSubprocesso: number,
    dados: { novaData: string },
): Promise<void> {
    await apiClient.post(`/processos/alterar-data-limite`, {id: codSubprocesso, ...dados});
}

export async function apresentarSugestoes(
    codProcesso: number,
    dados: { sugestoes: string },
): Promise<void> {
    await apiClient.post(`/processos/apresentar-sugestoes`, {id: codProcesso, ...dados});
}

export async function validarMapa(codProcesso: number): Promise<void> {
    await apiClient.post(`/processos/validar-mapa`, {id: codProcesso});
}

export async function buscarSubprocessos(
    codProcesso: number,
): Promise<Subprocesso[]> {
    const response = await apiClient.get(`/processos/${codProcesso}/subprocessos`);
    return response.data;
}
