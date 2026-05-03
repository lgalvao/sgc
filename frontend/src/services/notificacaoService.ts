import apiClient from "@/axios-setup";

export type StatusNotificacao =
    "PENDENTE"
    | "ENVIANDO"
    | "ENVIADO"
    | "FALHA_TEMPORARIA"
    | "FALHA_DEFINITIVA";

export interface Notificacao {
    codigo: number;
    subprocessoCodigo?: number;
    unidadeSigla?: string;
    unidadeDestinoSigla?: string;
    processoDescricao?: string;
    tipoNotificacao?: string;
    usuarioDestinoTitulo?: string;
    destinatario: string;
    assunto: string;
    corpoHtml?: string;
    situacao: StatusNotificacao;
    tentativas: number;
    dataHoraCriacao: string;
    dataHoraEnvio?: string;
    proximaTentativaEm?: string;
    ultimoErro?: string;
}

export const STATUS_NOTIFICACAO_INFO: Record<StatusNotificacao, { label: string; variant: string; prioridade: number }> = {
    ENVIADO:          { label: "Enviado",          variant: "success",   prioridade: 4 },
    PENDENTE:         { label: "Pendente",         variant: "secondary", prioridade: 2 },
    ENVIANDO:         { label: "Enviando...",       variant: "primary",   prioridade: 3 },
    FALHA_TEMPORARIA: { label: "Falha temporária", variant: "warning",   prioridade: 1 },
    FALHA_DEFINITIVA: { label: "Falha definitiva", variant: "danger",    prioridade: 0 },
};

export function obterStatusNotificacao(status: StatusNotificacao) {
    return STATUS_NOTIFICACAO_INFO[status];
}

export interface ReenvioNotificacaoResponse {
    codigo: number;
    reenfileiradas: number;
}

export async function listarNotificacoesAdmin(limite = 50): Promise<Notificacao[]> {
    const response = await apiClient.get<Notificacao[]>("/admin/notificacoes/listar", {
        params: { limite }
    });
    return response.data;
}

export async function reenviarNotificacao(codigo: number): Promise<ReenvioNotificacaoResponse> {
    const response = await apiClient.post<ReenvioNotificacaoResponse>(
        `/admin/notificacoes/${codigo}/reenviar`
    );
    return response.data;
}
