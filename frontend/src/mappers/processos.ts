import type {Processo, ProcessoResumo, UnidadeParticipante,} from "@/types/tipos";

export function mapProcessoResumoDtoToFrontend(dto: any): ProcessoResumo {
    return {
        ...dto,
    };
}

export function mapProcessoDtoToFrontend(dto: any): Processo {
    return {
        ...dto,
    };
}

export function mapUnidadeParticipanteDtoToFrontend(
    dto: any,
): UnidadeParticipante {
    return {
        ...dto,
        codUnidade: dto.codigo, // Mapear 'codigo' do DTO para 'codUnidade' no frontend
        filhos: dto.filhos
            ? dto.filhos.map(mapUnidadeParticipanteDtoToFrontend)
            : [],
    };
}

export function mapProcessoDetalheDtoToFrontend(dto: any): Processo {
    return {
        ...dto,
        unidades: dto.unidades
            ? dto.unidades.map(mapUnidadeParticipanteDtoToFrontend)
            : [],
        resumoSubprocessos: dto.resumoSubprocessos
            ? dto.resumoSubprocessos.map(mapProcessoResumoDtoToFrontend)
            : [],
    };
}
