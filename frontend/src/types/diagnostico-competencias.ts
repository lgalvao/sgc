// ──────────────────────────────────────────────────────────────────────────────
// Enums
// ──────────────────────────────────────────────────────────────────────────────

export type SituacaoAvaliacaoServidor =
    | 'AUTOAVALIACAO_NAO_INICIADA'
    | 'AUTOAVALIACAO_CONCLUIDA'
    | 'CONSENSO_CRIADO'
    | 'CONSENSO_APROVADO'
    | 'AVALIACAO_IMPOSSIBILITADA';

export type ValorSituacaoCapacitacao = 'NA' | 'AC' | 'EC' | 'C' | 'I';

export type SituacaoDiagnostico = 'EM_ANDAMENTO' | 'CONCLUIDO' | 'VALIDADO' | 'HOMOLOGADO';

// ──────────────────────────────────────────────────────────────────────────────
// Modelos de leitura (DTOs de resposta)
// ──────────────────────────────────────────────────────────────────────────────

export interface CompetenciaResumoDiag {
    competenciaCodigo: number;
    descricao: string;
}

/** Resultado por competência: código + notas de importância e domínio. */
export interface AvaliacaoCompetencia {
    competenciaCodigo: number;
    competenciaDescricao?: string | null;
    importancia: number | null;
    dominio: number | null;
}

export interface ConsensoCompetenciaDetalhada {
    competenciaCodigo: number;
    competenciaDescricao?: string | null;
    autoimportancia: number | null;
    autodominio: number | null;
    chefiaImportancia: number | null;
    chefiaDominio: number | null;
    consensoImportancia: number | null;
    consensoDominio: number | null;
}

/** Contexto do subprocesso de diagnóstico retornado pelo endpoint /contexto. */
export interface DiagnosticoContexto {
    processoCodigo: number;
    subprocessoCodigo: number;
    unidadeCodigo: number;
    unidadeSigla: string;
    unidadeNome: string;
    situacaoSubprocesso: string;
    situacaoDiagnostico: SituacaoDiagnostico;
    competencias: CompetenciaResumoDiag[];
}

/** Autoavaliação do servidor logado para o subprocesso. */
export interface Autoavaliacao {
    competencias: AvaliacaoCompetencia[];
    situacaoServidor: SituacaoAvaliacaoServidor;
    podeEditar: boolean;
    podeConcluirAutoavaliacao: boolean;
    habilitarConcluirAutoavaliacao: boolean;
}

/** Consenso da chefia para o servidor logado. */
export interface Consenso {
    servidorNome?: string | null;
    competencias: ConsensoCompetenciaDetalhada[];
    situacaoServidor: SituacaoAvaliacaoServidor;
    podeEditar: boolean;
    podeConcluirAvaliacao: boolean;
    habilitarConcluirAvaliacao: boolean;
    podeAprovarConsenso: boolean;
    habilitarAprovarConsenso: boolean;
}

/** Item da lista de servidores da equipe no diagnóstico. */
export interface ItemEquipeDiagnostico {
    servidorTitulo: string;
    servidorNome: string;
    situacaoServidor: SituacaoAvaliacaoServidor;
    podeManterConsenso: boolean;
    podeImpossibilitar: boolean;
    podePermitirAvaliacao: boolean;
}

/** Lista de servidores e suas situações na equipe. */
export interface DiagnosticoEquipe {
    servidores: ItemEquipeDiagnostico[];
}

/** Dados de um servidor no diagnóstico da unidade (visão da chefia/gestor). */
export interface ServidorDiagnostico {
    servidorTitulo: string;
    servidorNome: string;
    situacaoServidor: SituacaoAvaliacaoServidor;
    consenso: AvaliacaoCompetencia[];
    podeManterConsenso: boolean;
    podeImpossibilitar: boolean;
    podePermitirAvaliacao: boolean;
}

/** Situação de capacitação registrada para servidor/competência. */
export interface SituacaoCapacitacaoItem {
    competenciaCodigo: number;
    servidorTitulo: string;
    servidorNome?: string;
    situacaoCapacitacao: ValorSituacaoCapacitacao | null;
}

/** Resumo da unidade no diagnóstico. */
export interface UnidadeResumoDiag {
    unidadeCodigo: number;
    unidadeSigla: string;
    unidadeNome: string;
    situacaoSubprocesso: string;
    responsavelTitulo?: string | null;
}

/** Diagnóstico completo de uma unidade (servidores, situações de capacitação, histórico). */
export interface DiagnosticoUnidade {
    unidade: UnidadeResumoDiag;
    situacaoDiagnostico: SituacaoDiagnostico;
    servidores: ServidorDiagnostico[];
    situacoesCapacitacao: SituacaoCapacitacaoItem[];
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
    competencias: ConsensoCompetenciaDetalhada[];
}

export interface SituacaoCapacitacaoRequest {
    servidorTitulo: string;
    competenciaCodigo: number;
    situacaoCapacitacao: ValorSituacaoCapacitacao | null;
}

export interface SituacoesCapacitacaoRequest {
    situacoes: SituacaoCapacitacaoRequest[];
}

export interface JustificativaRequest {
    justificativa: string;
}

export interface TextoOpcionalRequest {
    texto?: string;
}
