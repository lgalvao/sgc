// noinspection JSUnusedGlobalSymbols

export enum TipoProcesso {
    MAPEAMENTO = 'Mapeamento',
    REVISAO = 'Revisão',
    DIAGNOSTICO = 'Diagnóstico',
}

export interface Processo {
    id: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimite: Date;
    situacao: string;
    dataFinalizacao: Date | null;
}

export interface Subprocesso {
    id: number;
    idProcesso: number;
    unidade: string;
    dataLimiteEtapa1: Date;
    dataLimiteEtapa2: Date | null;
    dataFimEtapa1: Date | null;
    dataFimEtapa2: Date | null;
    situacao: string;
    unidadeAtual: string;
    unidadeAnterior: string | null;
}

export interface Unidade {
    id: number;
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
    idProcesso: number;
    competencias: Competencia[];
    dataCriacao: Date;
    dataDisponibilizacao: Date | null;
    dataFinalizacao: Date | null;
}

export interface Conhecimento {
    id: number;
    descricao: string;
}

export interface Atividade {
    id: number;
    descricao: string;
    idSubprocesso: number;
    conhecimentos: Conhecimento[];
    novoConhecimento?: string;
}

export interface Alerta {
    id: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    dataHora: Date;
    idProcesso: number;
    descricao: string;
}

export interface AtribuicaoTemporaria {
    unidade: string;
    idServidor: number;
    dataInicio: Date;
    dataTermino: Date;
    justificativa: string;
}
