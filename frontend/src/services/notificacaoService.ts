import apiClient from "@/axios-setup";
import {STATUS_NOTIFICACAO_INFO} from "@/utils/statusHelpers";

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

export {STATUS_NOTIFICACAO_INFO, getNotificacaoStatusInfo as obterStatusNotificacao} from "@/utils/statusHelpers";

export interface ReenvioNotificacaoResponse {
    codigo: number;
    reenfileiradas: number;
}

export async function listarNotificacoesAdmin(limite = 50): Promise<Notificacao[]> {
    const response = await apiClient.get<Notificacao[]>("/admin/notificacoes/listar", {
        params: {limite}
    });
    return response.data;
}

export async function reenviarNotificacao(codigo: number): Promise<ReenvioNotificacaoResponse> {
    const response = await apiClient.post<ReenvioNotificacaoResponse>(
        `/admin/notificacoes/${codigo}/reenviar`
    );
    return response.data;
}

export function obterTimestampOrdenacao(item: Notificacao): number {
    const referencia = item.proximaTentativaEm || item.dataHoraEnvio || item.dataHoraCriacao;
    const timestamp = referencia ? Date.parse(referencia) : Number.NaN;
    return Number.isNaN(timestamp) ? 0 : timestamp;
}

export function compararNotificacoes(a: Notificacao, b: Notificacao): number {
    const infoA = STATUS_NOTIFICACAO_INFO[a.situacao];
    const infoB = STATUS_NOTIFICACAO_INFO[b.situacao];

    if (!infoA || !infoB) return 0;

    const prioridade = infoA.prioridade - infoB.prioridade;
    if (prioridade !== 0) {
        return prioridade;
    }
    return obterTimestampOrdenacao(b) - obterTimestampOrdenacao(a);
}

