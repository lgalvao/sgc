import type { Atividade, Conhecimento } from '@/types/tipos';

// Os DTOs do backend podem ter uma estrutura ligeiramente diferente.
// Por enquanto, vamos assumir que são semelhantes aos modelos do frontend.
// Se forem diferentes, essas funções de mapeamento farão a conversão.

export function mapAtividadeDtoToModel(dto: any): Atividade {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
    conhecimentos: dto.conhecimentos ? dto.conhecimentos.map(mapConhecimentoDtoToModel) : [],
  };
}

export function mapConhecimentoDtoToModel(dto: any): Conhecimento {
  return {
    codigo: dto.codigo,
    descricao: dto.descricao,
  };
}

export function mapCriarAtividadeRequestToDto(request: CriarAtividadeRequest): any {
  return {
    descricao: request.descricao,
    // O backend espera um mapa, então não incluímos conhecimentos aqui.
  };
}

export function mapCriarConhecimentoRequestToDto(request: CriarConhecimentoRequest): any {
    return {
        descricao: request.descricao,
    };
}