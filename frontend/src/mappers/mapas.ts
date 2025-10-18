import type { Atividade, Mapa, Conhecimento } from '@/types/tipos';
import type { MapaVisualizacao, AtividadeVisualizacao, ConhecimentoVisualizacao } from '@/types/tipos';

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