import { Competencia } from './competencia';
import { Subprocesso } from './subprocesso';

export interface Mapa {
    codigo: number;
    unidade: string;
    situacao: string;
    idProcesso: number;
    competencias: Competencia[];
    dataCriacao: Date;
    dataDisponibilizacao: Date | null;
    dataFinalizacao: Date | null;
    vigente?: boolean;
    dataInicio?: Date;
    dataFim?: Date | null;
    descricao?: string;
    tipo?: string;
    subprocessos?: Subprocesso[];
}