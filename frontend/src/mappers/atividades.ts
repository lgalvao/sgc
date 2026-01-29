import type {Atividade, Conhecimento, CriarConhecimentoRequest,} from "@/types/tipos";
import type {AtividadeDto, ConhecimentoDto} from "@/types/dtos";

export function mapAtividadeToModel(dto: AtividadeDto | null | undefined): Atividade | null {
    if (!dto) return null;
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        conhecimentos: (dto.conhecimentos || [])
            .map(mapConhecimentoToModel)
            .filter((c): c is Conhecimento => c !== null),
    };
}

// Alias for backwards compatibility
export const mapAtividadeVisualizacaoToModel = mapAtividadeToModel;

export function mapConhecimentoToModel(dto: ConhecimentoDto | null | undefined): Conhecimento | null {
    if (!dto) return null;
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
