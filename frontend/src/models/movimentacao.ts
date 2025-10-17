export interface Movimentacao {
    codigo: number;
    idSubprocesso: number;
    dataHora: Date;
    unidadeOrigem: string;
    unidadeDestino: string;
    descricao: string;
}