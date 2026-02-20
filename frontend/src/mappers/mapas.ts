import type {
    CompetenciaCompleta,
    CompetenciaImpactada,
    ImpactoMapa,
    Mapa,
    MapaAjuste,
    MapaCompleto,
} from "@/types/tipos";
import type {ImpactoMapaDto} from "@/types/dtos";

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
        competencias: (dto.competencias || []).map(
            (c: any): CompetenciaCompleta => ({
                codigo: c.codigo,
                descricao: c.descricao,
                atividadesAssociadas: c.atividadesCodigos || (c.atividades || []).map((a: any) => a.codigo) || [],
                atividades: (c.atividades || []).map((a: any) => ({
                    codigo: a.codigo,
                    descricao: a.descricao,
                    conhecimentos: (a.conhecimentos || []).map((k: any) => ({
                        codigo: k.codigo,
                        descricao: k.descricao,
                    })),
                })),
            }),
        ),
        situacao: dto.situacao || "",
    };
}

export function mapImpactoMapaDtoToModel(dto: ImpactoMapaDto): ImpactoMapa {
    const inseridas = dto.inseridas || [];
    const removidas = dto.removidas || [];
    const alteradas = dto.alteradas || [];
    const competencias = dto.competenciasImpactadas || [];

    return {
        temImpactos: dto.temImpactos,
        totalAtividadesInseridas: inseridas.length,
        totalAtividadesRemovidas: removidas.length,
        totalAtividadesAlteradas: alteradas.length,
        totalCompetenciasImpactadas: competencias.length,
        // Arrays de atividades impactadas - sem mapeamento trivial
        atividadesInseridas: inseridas,
        atividadesRemovidas: removidas,
        atividadesAlteradas: alteradas,
        competenciasImpactadas: competencias.map(
            (c: any): CompetenciaImpactada => ({
                codigo: c.codigo,
                descricao: c.descricao,
                atividadesAfetadas: c.atividadesAfetadas || [],
                tiposImpacto: c.tiposImpacto,
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
