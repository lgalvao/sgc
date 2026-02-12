import type {Atividade, AtividadeOperacaoResponse, Conhecimento, CriarConhecimentoRequest, SubprocessoStatus,} from "@/types/tipos";
import type {AtividadeDto, AtividadeOperacaoResponseDto, ConhecimentoDto, SubprocessoSituacaoDto} from "@/types/dtos";

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

export function mapSubprocessoSituacaoToModel(dto: SubprocessoSituacaoDto): SubprocessoStatus {
    return {
        codigo: dto.codigo,
        situacao: dto.situacao,
        situacaoLabel: dto.situacaoLabel,
    };
}

export function mapAtividadeOperacaoResponseToModel(dto: AtividadeOperacaoResponseDto): AtividadeOperacaoResponse {
    return {
        atividade: dto.atividade ? mapAtividadeToModel(dto.atividade) : null,
        subprocesso: mapSubprocessoSituacaoToModel(dto.subprocesso),
        atividadesAtualizadas: dto.atividadesAtualizadas
            .map(mapAtividadeToModel)
            .filter((a): a is Atividade => a !== null),
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
        descricao: request.descricao,
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
