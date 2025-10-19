export * from "@/types/tipos";


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

export function mapUnidadeParticipanteDtoToFrontend(dto: any): UnidadeParticipante {
    return {
        ...dto,
        filhos: dto.filhos ? dto.filhos.map(mapUnidadeParticipanteDtoToFrontend) : [],
    };
}

export function mapProcessoDetalheDtoToFrontend(dto: any): ProcessoDetalhe {
    return {
        ...dto,
        unidades: dto.unidades ? dto.unidades.map(mapUnidadeParticipanteDtoToFrontend) : [],
        resumoSubprocessos: dto.resumoSubprocessos ? dto.resumoSubprocessos.map(mapProcessoResumoDtoToFrontend) : [],
    };
}