export type DataISO = Date;
export type DataHoraISO = Date;

export enum ProcessoTipo {
    MAPEAMENTO = 'Mapeamento',
    REVISAO = 'Revisão',
    DIAGNOSTICO = 'Diagnóstico',
}

export interface Processo {
    id: number;
    descricao: string;
    tipo: ProcessoTipo;
    dataLimite: DataISO;
    situacao: string;
    dataFinalizacao?: DataISO | null;
}

export interface ProcessoUnidade {
    id: number;
    processoId: number;
    unidade: string;
    dataLimiteEtapa1: DataISO;
    dataLimiteEtapa2: DataISO;
    dataFimEtapa1: DataISO | null;
    dataFimEtapa2: DataISO | null;
    situacao: string;
    unidadeAtual: string;
    unidadeAnterior: string | null;
}

export interface Unidade {
    sigla: string;
    tipo: string;
    nome: string;
    titular: number;
    responsavel: number | null;
    filhas: Unidade[];
}

export interface Servidor {
    id: number;
    nome: string;
    unidade: string;
    email: string | null;
    ramal: string | null;
}

export interface Competencia {
    id: number;
    descricao: string;
    atividadesAssociadas: number[];
}

export interface Mapa {
    id: number;
    unidade: string;
    situacao: string;
    processoId: number;
    competencias: Competencia[];
    dataCriacao: DataISO;
    dataDisponibilizacao: DataISO | null;
    dataFinalizacao: DataISO | null;
}

export interface Conhecimento {
    id: number;
    descricao: string;
}

export interface Atividade {
    id: number;
    descricao: string;
    subprocessoId: number;
    conhecimentos: Conhecimento[];
    novoConhecimento?: string;
}

export interface Alerta {
    id: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    dataHora: DataHoraISO;
    processoId: number;
    descricao: string;
}

export interface AtribuicaoTemporaria {
    unidade: string;
    servidorId: number;
    dataInicio: DataISO;
    dataTermino: DataISO;
    justificativa: string;
}

