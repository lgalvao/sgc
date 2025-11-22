export enum TipoImpactoAtividade {
    INSERIDA = "INSERIDA",
    REMOVIDA = "REMOVIDA",
    ALTERADA = "ALTERADA",
}

export enum TipoImpactoCompetencia {
    ATIVIDADE_REMOVIDA = "ATIVIDADE_REMOVIDA",
    ATIVIDADE_ALTERADA = "ATIVIDADE_ALTERADA",
    IMPACTO_GENERICO = "IMPACTO_GENERICO",
}

export interface AtividadeImpactada {
    codigo: number;
    descricao: string;
    tipoImpacto: TipoImpactoAtividade;
    descricaoAnterior?: string;
    competenciasVinculadas: string[];
}

export interface CompetenciaImpactada {
    codigo: number;
    descricao: string;
    atividadesAfetadas: string[];
    tipoImpacto: TipoImpactoCompetencia;
}

export interface ImpactoMapa {
    temImpactos: boolean;
    totalAtividadesInseridas: number;
    totalAtividadesRemovidas: number;
    totalAtividadesAlteradas: number;
    totalCompetenciasImpactadas: number;
    atividadesInseridas: AtividadeImpactada[];
    atividadesRemovidas: AtividadeImpactada[];
    atividadesAlteradas: AtividadeImpactada[];
    competenciasImpactadas: CompetenciaImpactada[];
}
