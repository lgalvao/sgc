export interface SalvarCompetenciaRequest {
    descricao: string;
    atividadesCodigos: number[];
}

export interface CriarAtividadeRequest {
    descricao: string;
}

export interface CriarConhecimentoRequest {
    descricao: string;
}

export interface SalvarMapaRequest {
    competencias: {
        codigo?: number;
        descricao: string;
        atividades: {
            codigo?: number;
            descricao: string;
            conhecimentos: {
                codigo?: number;
                descricao: string;
            }[];
        }[];
    }[];
}

export interface SalvarAjustesRequest {
    competencias: {
        codigo: number;
        descricao: string;
    }[];
    atividades: {
        codigo: number;
        descricao: string;
    }[];
    sugestoes: string;
}

export interface DisponibilizarMapaRequest {
    dataLimite: string;
    observacoes: string;
}
