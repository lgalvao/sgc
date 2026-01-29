import type {Atividade, Conhecimento, CriarConhecimentoRequest,} from "@/types/tipos";

export function mapAtividadeToModel(dto: any): Atividade {
    if (!dto) return null as any;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: (dto.conhecimentos || []).map(mapConhecimentoToModel).filter((c: any) => c !== null),
    };
}

// Alias for backwards compatibility
export const mapAtividadeVisualizacaoToModel = mapAtividadeToModel;

export function mapConhecimentoToModel(dto: any): Conhecimento {
    if (!dto) return null as any;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

// Alias for backwards compatibility  
export const mapConhecimentoVisualizacaoToModel = mapConhecimentoToModel;

export function mapCriarAtividadeRequestToDto(
    request: any,
    codMapa: number,
): any {
    return {
        ...request,
        mapaCodigo: codMapa,
    };
}

export function mapAtualizarAtividadeToDto(request: Atividade): any {
    return {
        codigo: request.codigo,
        descricao: request.descricao,
        mapaCodigo: request.mapaCodigo,
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

export function mapAtualizarConhecimentoToDto(
    request: Conhecimento,
    atividadeCodigo: number
): any {
    return {
        codigo: request.codigo,
        atividadeCodigo: atividadeCodigo,
        descricao: request.descricao,
    };
}
