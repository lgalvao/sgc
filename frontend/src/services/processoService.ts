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

export async function executarAcaoEmBloco(
    codProcesso: number,
    payload: { unidadeCodigos: number[]; acao: "aceitar" | "homologar" | "disponibilizar"; dataLimite?: string },
): Promise<void> {
    await apiClient.post(`/processos/${codProcesso}/acao-em-bloco`, {
        ...payload,
        acao: payload.acao.toUpperCase(),
    });
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
    await apiClient.post(`/subprocessos/${codSubprocesso}/data-limite`, {
        novaDataLimite: dados.novaData
    });
}

export async function apresentarSugestoes(
    codSubprocesso: number,
    dados: { sugestoes: string },
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/apresentar-sugestoes`, dados);
}

export async function validarMapa(codSubprocesso: number): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/validar-mapa`);
}

export async function homologarValidacao(codSubprocesso: number): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/homologar-validacao`);
}

export async function aceitarValidacao(codSubprocesso: number, dados?: { observacoes?: string }): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/aceitar-validacao`, dados || {});
}

export async function buscarSubprocessos(
    codProcesso: number,
): Promise<Subprocesso[]> {
    const response = await apiClient.get(`/processos/${codProcesso}/subprocessos`);
    return response.data;
}

export async function buscarContextoCompleto(codProcesso: number) {
    const response = await apiClient.get(`/processos/${codProcesso}/contexto-completo`);
    return response.data;
}

// CDU-32: Reabrir cadastro
export async function reabrirCadastro(
    codSubprocesso: number,
    justificativa: string,
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/reabrir-cadastro`, { justificativa });
}

// CDU-33: Reabrir revisão de cadastro
export async function reabrirRevisaoCadastro(
    codSubprocesso: number,
    justificativa: string,
): Promise<void> {
    await apiClient.post(`/subprocessos/${codSubprocesso}/reabrir-revisao-cadastro`, { justificativa });
}

// CDU-34: Enviar lembrete de prazo
export async function enviarLembrete(
    codProcesso: number,
    unidadeCodigo: number,
): Promise<void> {
    await apiClient.post(`/processos/${codProcesso}/enviar-lembrete`, { unidadeCodigo });
}
