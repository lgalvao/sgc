import type {
    AtividadeImpactada,
    CompetenciaImpactada,
    ImpactoMapa,
    Mapa,
    MapaAjuste,
    MapaCompleto,
} from "@/types/tipos";

export function mapMapaDtoToModel(dto: any): Mapa {
    return {
        codigo: dto.codigo,
        codProcesso: dto.codProcesso,
        unidade: dto.unidade,
        situacao: dto.situacao,
        dataCriacao: dto.dataCriacao,
        dataDisponibilizacao: dto.dataDisponibilizacao,
        dataFinalizacao: dto.dataFinalizacao,
        competencias: dto.competencias || [],
        descricao: dto.descricao,
    };
}

export function mapMapaCompletoDtoToModel(dto: any): MapaCompleto {
    return {
        codigo: dto.codigo,
        subprocessoCodigo: dto.subprocessoCodigo,
        observacoes: dto.observacoes,
        competencias: (dto.competencias || []).map((c: any) => ({
            codigo: c.codigo,
            descricao: c.descricao,
            atividadesAssociadas: c.atividadesCodigos || [],
        })),
        situacao: dto.situacao || "",
    };
}

export function mapImpactoMapaDtoToModel(dto: any): ImpactoMapa {
    return {
        temImpactos: dto.temImpactos,
        totalAtividadesInseridas: dto.totalAtividadesInseridas,
        totalAtividadesRemovidas: dto.totalAtividadesRemovidas,
        totalAtividadesAlteradas: dto.totalAtividadesAlteradas,
        totalCompetenciasImpactadas: dto.totalCompetenciasImpactadas,
        atividadesInseridas: (dto.atividadesInseridas || []).map(
            (a: any): AtividadeImpactada => ({
                ...a,
            }),
        ),
        atividadesRemovidas: (dto.atividadesRemovidas || []).map(
            (a: any): AtividadeImpactada => ({
                ...a,
            }),
        ),
        atividadesAlteradas: (dto.atividadesAlteradas || []).map(
            (a: any): AtividadeImpactada => ({
                ...a,
            }),
        ),
        competenciasImpactadas: (dto.competenciasImpactadas || []).map(
            (c: any): CompetenciaImpactada => ({
                codigo: c.codigo,
                descricao: c.descricao,
                atividadesAfetadas: c.atividadesAfetadas || [],
                tipoImpacto: c.tipoImpacto,
            }),
        ),
    };
}

export function mapMapaAjusteDtoToModel(dto: any): MapaAjuste {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        competencias: dto.competencias || [],
    };
}
