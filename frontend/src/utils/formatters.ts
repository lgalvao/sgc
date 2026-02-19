
import { SituacaoProcesso, TipoProcesso, SituacaoSubprocesso } from '@/types/tipos';

export function formatDate(date: string | Date | undefined | null, includeTime = true): string {
  if (!date) return '';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '';

  const options: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  };

  if (includeTime) {
    options.hour = '2-digit';
    options.minute = '2-digit';
  }

  return new Intl.DateTimeFormat('pt-BR', options).format(d);
}

export function formatSituacaoProcesso(situacao: SituacaoProcesso | string | undefined | null): string {
  if (!situacao) return '';
  const labels: Record<string, string> = {
    [SituacaoProcesso.CRIADO]: 'Criado',
    [SituacaoProcesso.EM_ANDAMENTO]: 'Em andamento',
    [SituacaoProcesso.FINALIZADO]: 'Finalizado',
  };
  return labels[situacao] || situacao;
}

export function formatTipoProcesso(tipo: TipoProcesso | string | undefined | null): string {
  if (!tipo) return '';
  const labels: Record<string, string> = {
    [TipoProcesso.MAPEAMENTO]: 'Mapeamento',
    [TipoProcesso.REVISAO]: 'Revisão',
    [TipoProcesso.DIAGNOSTICO]: 'Diagnóstico',
  };
  return labels[tipo] || tipo;
}

export function formatSituacaoSubprocesso(situacao: SituacaoSubprocesso | string | undefined | null): string {
  if (!situacao) return '';
  const labels: Record<string, string> = {
    [SituacaoSubprocesso.NAO_INICIADO]: 'Não iniciado',
    [SituacaoSubprocesso.MAPEAMENTO_CADASTRO_EM_ANDAMENTO]: 'Cadastro em andamento',
    [SituacaoSubprocesso.MAPEAMENTO_CADASTRO_DISPONIBILIZADO]: 'Cadastro disponibilizado',
    [SituacaoSubprocesso.MAPEAMENTO_CADASTRO_HOMOLOGADO]: 'Cadastro homologado',
    [SituacaoSubprocesso.MAPEAMENTO_MAPA_CRIADO]: 'Mapa criado',
    [SituacaoSubprocesso.MAPEAMENTO_MAPA_DISPONIBILIZADO]: 'Mapa disponibilizado',
    [SituacaoSubprocesso.MAPEAMENTO_MAPA_COM_SUGESTOES]: 'Mapa com sugestões',
    [SituacaoSubprocesso.MAPEAMENTO_MAPA_VALIDADO]: 'Mapa validado',
    [SituacaoSubprocesso.MAPEAMENTO_MAPA_HOMOLOGADO]: 'Mapa homologado',
    [SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO]: 'Revisão em andamento',
    [SituacaoSubprocesso.REVISAO_CADASTRO_DISPONIBILIZADA]: 'Revisão disponibilizada',
    [SituacaoSubprocesso.REVISAO_CADASTRO_HOMOLOGADA]: 'Revisão homologada',
    [SituacaoSubprocesso.REVISAO_MAPA_AJUSTADO]: 'Mapa ajustado',
    [SituacaoSubprocesso.REVISAO_MAPA_DISPONIBILIZADO]: 'Mapa disponibilizado',
    [SituacaoSubprocesso.REVISAO_MAPA_COM_SUGESTOES]: 'Mapa com sugestões',
    [SituacaoSubprocesso.REVISAO_MAPA_VALIDADO]: 'Mapa validado',
    [SituacaoSubprocesso.REVISAO_MAPA_HOMOLOGADO]: 'Mapa homologado',
  };
  return labels[situacao] || situacao;
}
