export enum TipoResponsabilidade {
    SUBSTITUICAO = 'Substituição',
    ATRIBUICAO = 'Atribuição temporária'
}

export interface Responsavel {
    idServidor: number;
    tipo: TipoResponsabilidade;
    dataInicio: Date | null;
    dataFim: Date | null;
}

export interface Unidade {
    codigo: number;
    sigla: string;
    tipo: string;
    nome: string;
    idServidorTitular: number;
    responsavel: Responsavel | null;
    filhas: Unidade[];
}