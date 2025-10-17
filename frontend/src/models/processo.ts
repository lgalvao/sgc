export enum TipoProcesso {
    MAPEAMENTO = 'Mapeamento',
    REVISAO = 'Revisão',
    DIAGNOSTICO = 'Diagnóstico',
}

export enum SituacaoProcesso {
    CRIADO = 'Criado',
    EM_ANDAMENTO = 'Em andamento',
    FINALIZADO = 'Finalizado',
}

export interface UnidadeSnapshot {
    sigla: string;
    tipo: string;
    filhas: UnidadeSnapshot[];
}

export interface Processo {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimite: Date;
    situacao: SituacaoProcesso;
    dataFinalizacao: Date | null;
    unidadesSnapshot?: UnidadeSnapshot[]; // Cópia da árvore/unidades participantes no momento da iniciação
}