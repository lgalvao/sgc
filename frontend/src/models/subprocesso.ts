import { SITUACOES_SUBPROCESSO } from '@/constants/situacoes';
import { Movimentacao } from './movimentacao';
import { AnaliseValidacao } from './analise';

export interface Subprocesso {
    codigo: number;
    idProcesso: number;
    unidade: string;
    situacao: typeof SITUACOES_SUBPROCESSO[keyof typeof SITUACOES_SUBPROCESSO];
    unidadeAtual: string;
    unidadeAnterior: string | null;
    dataLimiteEtapa1: Date;
    dataFimEtapa1: Date | null;
    dataLimiteEtapa2: Date | null;
    dataFimEtapa2: Date | null;
    sugestoes?: string;
    observacoes?: string;
    movimentacoes: Movimentacao[];
    analises: AnaliseValidacao[];
    idMapaCopiado?: number;
}