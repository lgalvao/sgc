// ──────────────────────────────────────────────────────────────────────────────
// Enums
// ──────────────────────────────────────────────────────────────────────────────

export type SituacaoAvaliacaoServidor =
    | 'AUTOAVALIACAO_NAO_REALIZADA'
    | 'AUTOAVALIACAO_CONCLUIDA'
    | 'CONSENSO_CRIADO'
    | 'CONSENSO_APROVADO'
    | 'AVALIACAO_IMPOSSIBILITADA';

export type SituacaoCapacitacao = 'NA' | 'AC' | 'EC' | 'C' | 'I';

export type SituacaoDiagnostico = 'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO';

// ──────────────────────────────────────────────────────────────────────────────
// Modelos de leitura (DTOs de resposta)
// ──────────────────────────────────────────────────────────────────────────────

export interface CompetenciaResumoDiag {
    codigo: number;
    descricao: string;
}

/** Resultado por competência: código + notas de importância e domínio. */
export interface AvaliacaoCompetencia {
    competenciaCodigo: number;
    importancia: number | null;
    dominio: number | null;
}

/** Contexto do subprocesso de diagnóstico retornado pelo endpoint /contexto. */
export interface DiagnosticoContexto {
    processoCodigo: number;
    subprocessoCodigo: number;
    unidadeCodigo: number;
    unidadeSigla: string;
    unidadeNome: string;
    situacaoSubprocesso: string;
    competencias: CompetenciaResumoDiag[];
}

/** Autoavaliação do servidor logado para o subprocesso. */
export interface Autoavaliacao {
    competencias: AvaliacaoCompetencia[];
    situacaoServidor: SituacaoAvaliacaoServidor;
}

/** Consenso da chefia para o servidor logado. */
export interface Consenso {
    competencias: AvaliacaoCompetencia[];
    situacaoServidor: SituacaoAvaliacaoServidor;
}

/** Item da lista de servidores da equipe no diagnóstico. */
export interface ItemEquipeDiagnostico {
    titulo: string;
    nome: string;
    situacaoServidor: SituacaoAvaliacaoServidor;
}

/** Lista de servidores e suas situações na equipe. */
export interface DiagnosticoEquipe {
    itens: ItemEquipeDiagnostico[];
}

/** Dados de um servidor no diagnóstico da unidade (visão da chefia/gestor). */
export interface ServidorDiagnostico {
    titulo: string;
    nome: string;
    situacaoServidor: SituacaoAvaliacaoServidor;
    competencias: AvaliacaoCompetencia[];
}

/** Ocupação crítica registrada para servidor/competência. */
export interface OcupacaoCriticaItem {
    competenciaCodigo: number;
    servidorTitulo: string;
    situacaoCapacitacao: SituacaoCapacitacao;
}

/** Resumo da unidade no diagnóstico. */
export interface UnidadeResumoDiag {
    codigo: number;
    sigla: string;
    nome: string;
    situacao: string;
}

/** Diagnóstico completo de uma unidade (servidores, ocupações, histórico). */
export interface DiagnosticoUnidade {
    unidade: UnidadeResumoDiag;
    servidores: ServidorDiagnostico[];
    ocupacoesCriticas: OcupacaoCriticaItem[];
    movimentacoes: MovimentacaoDiag[];
}

/** Movimentação do histórico do subprocesso. */
export interface MovimentacaoDiag {
    descricao: string;
    dataHora: string;
    unidadeOrigem: string;
    unidadeDestino: string;
    usuario?: string;
}

// ──────────────────────────────────────────────────────────────────────────────
// Requests (DTOs de envio)
// ──────────────────────────────────────────────────────────────────────────────

export interface AutoavaliacaoRequest {
    competencias: AvaliacaoCompetencia[];
}

export interface ConsensoRequest {
    competencias: AvaliacaoCompetencia[];
    motivoReabertura?: string;
}

export interface OcupacaoCriticaRequest {
    servidorTitulo: string;
    competenciaCodigo: number;
    situacaoCapacitacao: SituacaoCapacitacao;
}

export interface OcupacoesCriticasRequest {
    ocupacoes: OcupacaoCriticaRequest[];
}

export interface JustificativaRequest {
    justificativa: string;
}

export interface TextoOpcionalRequest {
    texto?: string;
}
