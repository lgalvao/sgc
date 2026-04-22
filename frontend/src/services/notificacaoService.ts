import apiClient from "@/axios-setup";
import type {SituacaoSubprocesso} from "@/types/tipos";

export type StatusGeralNotificacao =
    "INCONSISTENTE"
    | "OK"
    | "PENDENTE"
    | "FALHA_TEMPORARIA"
    | "FALHA_DEFINITIVA";

export interface NotificacaoSubprocessoResumo {
    subprocessoCodigo: number;
    processoCodigo: number;
    processoDescricao: string;
    unidadeSigla: string;
    situacaoSubprocesso: SituacaoSubprocesso;
    totalNotificacoes: number;
    pendentes: number;
    enviando: number;
    enviadas: number;
    falhasTemporarias: number;
    falhasDefinitivas: number;
    statusGeral: StatusGeralNotificacao;
    ultimaNotificacaoEm: string | null;
    proximaTentativaEm: string | null;
    maiorTentativas: number;
    ultimoErro: string | null;
    podeReenviar: boolean;
}

export interface ReenvioNotificacaoResponse {
    subprocessoCodigo: number;
    reenfileiradas: number;
}

export async function listarResumoSubprocessosAtivos(): Promise<NotificacaoSubprocessoResumo[]> {
    const response = await apiClient.get<NotificacaoSubprocessoResumo[]>("/admin/notificacoes/subprocessos-ativos");
    return response.data;
}

export async function reenviarFalhasDefinitivas(subprocessoCodigo: number): Promise<ReenvioNotificacaoResponse> {
    const response = await apiClient.post<ReenvioNotificacaoResponse>(
        `/admin/notificacoes/subprocessos/${subprocessoCodigo}/reenviar`
    );
    return response.data;
}
