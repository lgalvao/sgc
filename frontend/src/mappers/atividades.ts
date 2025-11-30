import type { Atividade, Conhecimento, CriarConhecimentoRequest, } from "@/types/tipos";

export function mapAtividadeDtoToModel(dto: any): Atividade {
  if (!dto) return null as any;
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    conhecimentos: dto.conhecimentos
      ? dto.conhecimentos.map(mapConhecimentoDtoToModel).filter((c: any) => c !== null)
      : [],
  };
}

export function mapConhecimentoDtoToModel(dto: any): Conhecimento {
  if (!dto) return null as any;
  return {
    id: dto.codigo,
    descricao: dto.descricao,
  };
}

export function mapCriarAtividadeRequestToDto(
  request: any,
  codSubrocesso: number,
): any {
  return {
    ...request,
    mapaCodigo: codSubrocesso,
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
