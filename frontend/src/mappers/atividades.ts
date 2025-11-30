import type { Atividade, Conhecimento, CriarConhecimentoRequest, } from "@/types/tipos";

export function mapAtividadeDtoToModel(dto: any): Atividade {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    conhecimentos: dto.conhecimentos
      ? dto.conhecimentos.map(mapConhecimentoDtoToModel)
      : [],
  };
}

export function mapConhecimentoDtoToModel(dto: any): Conhecimento {
  return {
    id: dto.id,
    descricao: dto.descricao,
  };
}

export function mapCriarAtividadeRequestToDto(
  request: any,
  mapaCodigo: number,
): any {
  return {
    ...request,
    mapaCodigo,
  };
}

export function mapCriarConhecimentoRequestToDto(
  request: CriarConhecimentoRequest,
  atividadeCodigo: number,
): any {
  return {
    descricao: request.descricao,
    atividadeCodigo,
  };
}
