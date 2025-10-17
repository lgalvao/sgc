export interface Alerta {
    codigo: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    dataHora: Date;
    idProcesso: number;
    descricao: string;
}

export interface AlertaServidor {
    codigo: number;
    idAlerta: number;
    idServidor: number;
    lido: boolean;
    dataLeitura: Date | null;
}