import {formatarDataHoraBR} from "@/utils";
import {TIPOS_NOTIFICACAO_LABELS} from "@/constants/notificacoes";
import type {Notificacao} from "@/services/notificacaoService";

export function formatarDataOuHifen(valor?: string | null): string {
  if (!valor) return "-";
  const formatada = formatarDataHoraBR(valor);
  return formatada === "Não informado" || formatada === "Data inválida" ? "-" : formatada;
}

export function resumirContexto(item: Notificacao): string {
  return [
    item.processoDescricao,
    item.usuarioDestinoTitulo ? `Título ${item.usuarioDestinoTitulo}` : null,
  ].filter(Boolean).join(" • ") || "Sem contexto adicional";
}

export function formatarTipoNotificacao(tipo?: string): string {
  if (!tipo) return "-";
  return TIPOS_NOTIFICACAO_LABELS[tipo] || tipo;
}

export function formatarDestinatario(item: Partial<Notificacao> & { destinatario: string }): string {
  if (item.usuarioDestinoTitulo?.trim()) {
    return item.destinatario.trim();
  }
  if (item.unidadeSigla?.trim()) {
    return item.unidadeSigla.trim().toUpperCase();
  }
  const correspondenciaEmailInstitucional = item.destinatario.trim().match(/^([^@]+)@tre-pe\.jus\.br$/i);
  if (correspondenciaEmailInstitucional?.[1]) {
    return correspondenciaEmailInstitucional[1].toUpperCase();
  }
  return item.destinatario.trim();
}

export function formatarAssunto(assunto?: string): string {
  return assunto?.replace(/^SGC:\s*/i, "").trim() || "-";
}

export function formatarQuando(item: Notificacao): string {
  if (item.situacao === "ENVIADO") {
    return formatarDataOuHifen(item.dataHoraEnvio);
  }
  return formatarDataOuHifen(item.proximaTentativaEm || item.dataHoraCriacao);
}
