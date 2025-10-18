import type {
    Atividade,
    Mapa,
    Conhecimento,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
    AtividadeVisualizacao,
    ConhecimentoVisualizacao
} from '@/types/tipos';

function mapConhecimentoVisualizacaoToModel(dto: ConhecimentoVisualizacao): Conhecimento {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

function mapAtividadeVisualizacaoToModel(dto: AtividadeVisualizacao): Atividade {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: dto.conhecimentos.map(mapConhecimentoVisualizacaoToModel),
    };
}

export function mapMapaVisualizacaoToAtividades(dto: MapaVisualizacao): Atividade[] {
    if (!dto || !dto.competencias) {
        return [];
    }
    const todasAtividades = dto.competencias.flatMap(competencia => competencia.atividades || []);
    return todasAtividades.map(mapAtividadeVisualizacaoToModel);
}

export function mapMapaDtoToModel(dto: any): Mapa {
    return {
        id: dto.id,
        idProcesso: dto.idProcesso,
        unidade: dto.unidade,
        situacao: dto.situacao,
        dataCriacao: new Date(dto.dataCriacao),
        dataDisponibilizacao: dto.dataDisponibilizacao ? new Date(dto.dataDisponibilizacao) : null,
        dataFinalizacao: dto.dataFinalizacao ? new Date(dto.dataFinalizacao) : null,
        competencias: dto.competencias || [],
    };
}

export function mapMapaCompletoDtoToModel(dto: any): MapaCompleto {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        competencias: (dto.competencias || []).map((c: any) => ({
            codigo: c.codigo,
            descricao: c.descricao,
            atividades: (c.atividades || []).map((a: any) => ({
                codigo: a.codigo,
                descricao: a.descricao,
                conhecimentos: (a.conhecimentos || []).map((con: any) => ({
                    codigo: con.codigo,
                    descricao: con.descricao,
                })),
            })),
        })),
    };
}

export function mapImpactoMapaDtoToModel(dto: any): ImpactoMapa {
    return {
        temImpacto: dto.temImpacto,
        competencias: (dto.competencias || []).map((c: any) => ({
            id: c.id,
            descricao: c.descricao,
            atividadesAdicionadas: c.atividadesAdicionadas || [],
            atividadesRemovidas: c.atividadesRemovidas || [],
            conhecimentosAdicionados: c.conhecimentosAdicionados || [],
            conhecimentosRemovidos: c.conhecimentosRemovidos || [],
        })),
    };
}

export function mapMapaAjusteDtoToModel(dto: any): MapaAjuste {
    return {
        competencias: dto.competencias || [],
        sugestoes: dto.sugestoes || '',
    };
}