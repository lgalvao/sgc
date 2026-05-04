export interface GrupoViolacaoOrganizacional {
    tipo: string;
    quantidadeOcorrencias: number;
    ocorrencias: string[];
}

export interface DiagnosticoOrganizacional {
    possuiViolacoes: boolean;
    resumo: string;
    quantidadeTiposViolacao: number;
    quantidadeOcorrencias: number;
    grupos: GrupoViolacaoOrganizacional[];
}
