import type {Processo, UnidadeParticipante} from "@/types/tipos";
import type {ProcessoDetalheDto, UnidadeParticipanteDto} from "@/types/dtos";

export function mapUnidadeParticipanteDtoToFrontend(
    dto: UnidadeParticipanteDto,
): UnidadeParticipante {
    return {
        ...dto,
        codUnidade: dto.codigo,
        codSubprocesso: dto.codSubprocesso || 0,
        situacaoSubprocesso: (dto.situacaoSubprocesso as any) || 'CRIADO',
        dataLimite: dto.dataLimite || '',
        filhos: dto.filhos
            ? dto.filhos.map(mapUnidadeParticipanteDtoToFrontend)
            : [],
    } as UnidadeParticipante;
}

export function mapProcessoDetalheDtoToFrontend(dto: ProcessoDetalheDto): Processo {
    return {
        ...dto,
        unidades: dto.unidades
            ? dto.unidades.map(mapUnidadeParticipanteDtoToFrontend)
            : [],
        resumoSubprocessos: (dto as any).resumoSubprocessos || [],
    } as Processo;
}
