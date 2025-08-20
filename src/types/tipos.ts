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

export interface Processo {
    id: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimite: Date;
    situacao: SituacaoProcesso;
    dataFinalizacao: Date | null;
}

export interface Subprocesso {
    id: number;
    idProcesso: number;
    unidade: string;
    situacao: string;
    unidadeAtual: string;
    unidadeAnterior: string | null;
    dataLimiteEtapa1: Date;
    dataFimEtapa1: Date | null;
    dataLimiteEtapa2: Date | null;
    dataFimEtapa2: Date | null;
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
