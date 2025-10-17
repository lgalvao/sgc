export enum ResultadoAnalise {
    DEVOLUCAO = 'Devolução',
    ACEITE = 'Aceite',
}

export interface AnaliseValidacao {
    codigo: number;
    idSubprocesso: number;
    dataHora: Date;
    unidade: string;
    resultado: ResultadoAnalise; // Usar o enum
    observacao?: string;
}