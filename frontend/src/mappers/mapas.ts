import type {
    Atividade,
    AtividadeVisualizacao,
    Competencia,
    Conhecimento,
    ConhecimentoVisualizacao,
    ImpactoMapa,
    Mapa,
    MapaAjuste,
    MapaCompleto,
    MapaVisualizacao,
} from '@/types/tipos';

function mapConhecimentoVisualizacaoToModel(dto: ConhecimentoVisualizacao): Conhecimento {
  return {
    id: dto.id,
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
    codigo: dto.codigo,
    idProcesso: dto.idProcesso,
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
      atividades: (c.atividades || []).map((a: any) => ({
        codigo: a.codigo,
        descricao: a.descricao,
        conhecimentos: (a.conhecimentos || []).map((con: any) => ({
          id: con.id,
          descricao: con.descricao,
        })),
      })),
    })),
  };
}

export function mapImpactoMapaDtoToModel(dto: any): ImpactoMapa {
  return {
    temImpactos: dto.temImpactos,
    totalAtividadesInseridas: dto.totalAtividadesInseridas,
    totalAtividadesRemovidas: dto.totalAtividadesRemovidas,
    totalAtividadesAlteradas: dto.totalAtividadesAlteradas,
    totalCompetenciasImpactadas: dto.totalCompetenciasImpactadas,
    atividadesInseridas: dto.atividadesInseridas || [],
    atividadesRemovidas: dto.atividadesRemovidas || [],
    atividadesAlteradas: dto.atividadesAlteradas || [],
    competenciasImpactadas: (dto.competenciasImpactadas || []).map((c: any): Competencia => ({
      codigo: c.codigo,
      descricao: c.descricao,
      atividadesAssociadas: c.atividadesAssociadas || [],
    })),
  };
}

export function mapMapaAjusteDtoToModel(dto: any): MapaAjuste {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    competencias: dto.competencias || [],
  };
}