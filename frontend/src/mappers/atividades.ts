import type {Atividade, Conhecimento, CriarConhecimentoRequest,} from "@/types/tipos";

export function mapAtividadeVisualizacaoToModel(dto: any): Atividade {
    if (!dto) return null as any;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: (dto.conhecimentos || []).map(mapConhecimentoVisualizacaoToModel).filter((c: any) => c !== null),
    };
}

export function mapConhecimentoVisualizacaoToModel(dto: any): Conhecimento {
    if (!dto) return null as any;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

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
        codigo: dto.codigo,
        descricao: dto.descricao,
    };
}

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
