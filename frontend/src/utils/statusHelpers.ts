import {SituacaoProcesso, SituacaoSubprocesso, StatusNotificacao} from "@/types/tipos";

/**
 * Utilitário para centralizar mapeamentos visuais de status (badges, variantes e prioridades).
 */

/**
 * Retorna a variante do Bootstrap para um status de Processo.
 */
export function getProcessoBadgeVariant(situacao: SituacaoProcesso | string | undefined | null): string {
  if (!situacao) return "secondary";
  switch (situacao) {
    case SituacaoProcesso.FINALIZADO:
      return "success";
    case SituacaoProcesso.EM_ANDAMENTO:
      return "primary";
    case SituacaoProcesso.CRIADO:
      return "secondary";
    default:
      return "dark";
  }
}

/**
 * Retorna a variante do Bootstrap para um status de Subprocesso.
 */
export function getSubprocessoBadgeVariant(situacao: SituacaoSubprocesso | string | undefined | null): string {
  if (!situacao) return "secondary";

  // Mapeamento baseado no progresso do workflow
  if (situacao.includes("HOMOLOGADO") || situacao.includes("HOMOLOGADA")) return "success";
  if (situacao.includes("VALIDADO") || situacao.includes("VALIDADA")) return "info";
  if (situacao.includes("DISPONIBILIZADO") || situacao.includes("DISPONIBILIZADA")) return "warning";
  if (situacao.includes("EM_ANDAMENTO")) return "primary";
  if (situacao === SituacaoSubprocesso.NAO_INICIADO) return "secondary";

  return "primary";
}

/**
 * Informações visuais e de ordenação para status de Notificação.
 */
export const STATUS_NOTIFICACAO_INFO: Record<string, { label: string; variant: string; prioridade: number }> = {
  "PENDENTE": {label: "Pendente", variant: "warning", prioridade: 2},
  "ENVIANDO": {label: "Enviando", variant: "info", prioridade: 1},
  "ENVIADO": {label: "Enviado", variant: "success", prioridade: 4},
  "FALHA_TEMPORARIA": {label: "Falha Temporária", variant: "danger", prioridade: 3},
  "FALHA_DEFINITIVA": {label: "Falha Definitiva", variant: "danger", prioridade: 0},
};

/**
 * Retorna o objeto de informações completo para um status de Notificação.
 */
export function getNotificacaoStatusInfo(situacao: string): { label: string; variant: string; prioridade: number } {
  return STATUS_NOTIFICACAO_INFO[situacao] || { label: situacao, variant: "secondary", prioridade: -1 };
}

/**
 * Retorna a variante do Bootstrap para um status de Notificação.
 */
export function getNotificacaoBadgeVariant(situacao: string): string {
  return STATUS_NOTIFICACAO_INFO[situacao]?.variant || "secondary";
}
