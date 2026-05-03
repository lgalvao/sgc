import {SituacaoSubprocesso, type UnidadeParticipante} from "@/types/tipos";
import type {UnidadeParticipanteDto} from "@/types/dtos";
import type {Processo, ProcessoDetalheResponseBackend} from "./types";

export function mapearUnidadeParticipante(dto: UnidadeParticipanteDto): UnidadeParticipante {
    return {
        nome: dto.nome!,
        sigla: dto.sigla!,
        codUnidade: dto.codUnidade,
        codSubprocesso: dto.codSubprocesso ?? 0,
        codUnidadeSuperior: dto.codUnidadeSuperior,
        situacaoSubprocesso: (dto.situacaoSubprocesso as SituacaoSubprocesso | undefined) ?? SituacaoSubprocesso.NAO_INICIADO,
        dataLimite: dto.dataLimite ?? "",
        mapaCodigo: dto.mapaCodigo,
        localizacaoAtualCodigo: dto.localizacaoAtualCodigo,
        filhos: (dto.filhos ?? []).map(mapearUnidadeParticipante),
    };
}

export function mapearProcessoDetalhe(dto: ProcessoDetalheResponseBackend): Processo {
    return {
        codigo: dto.codigo,
        descricao: dto.descricao,
        tipo: dto.tipo,
        situacao: dto.situacao,
        dataLimite: dto.dataLimite,
        dataCriacao: dto.dataCriacao,
        dataFinalizacao: dto.dataFinalizacao,
        podeFinalizar: dto.podeFinalizar,
        podeHomologarCadastro: dto.podeHomologarCadastro,
        podeHomologarMapa: dto.podeHomologarMapa,
        podeAceitarCadastroBloco: dto.podeAceitarCadastroBloco,
        podeDisponibilizarMapaBloco: dto.podeDisponibilizarMapaBloco,
        unidades: dto.unidades.map(mapearUnidadeParticipante),
        resumoSubprocessos: dto.resumoSubprocessos,
        acoesBloco: dto.acoesBloco,
    };
}
