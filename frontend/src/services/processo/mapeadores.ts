import {SituacaoSubprocesso, type UnidadeParticipante} from "@/types/tipos";
import type {Processo, ProcessoDetalheResponseBackend, UnidadeImportacao, UnidadeParticipanteDto} from "./types";

function converterParaSituacao(valor: string | null): SituacaoSubprocesso | null {
    if (!valor) return null;
    return valor as SituacaoSubprocesso;
}

function mapearUnidadeParticipante(dto: UnidadeParticipanteDto): UnidadeParticipante {
    return {
        nome: dto.nome,
        sigla: dto.sigla,
        codUnidade: dto.codUnidade,
        codSubprocesso: dto.codSubprocesso ?? null,
        codUnidadeSuperior: dto.codUnidadeSuperior ?? undefined,
        situacaoSubprocesso: converterParaSituacao(dto.situacaoSubprocesso),
        dataLimite: dto.dataLimite ?? "",
        mapaCodigo: dto.mapaCodigo ?? undefined,
        localizacaoAtualCodigo: dto.localizacaoAtualCodigo ?? undefined,
        filhos: dto.filhos.map(mapearUnidadeParticipante),
    };
}

export function mapearUnidadeImportacao(dto: UnidadeParticipanteDto): UnidadeImportacao {
    return {
        nome: dto.nome,
        sigla: dto.sigla,
        codUnidade: dto.codUnidade,
        codSubprocesso: dto.codSubprocesso ?? null,
        codUnidadeSuperior: dto.codUnidadeSuperior ?? undefined,
        situacaoSubprocesso: converterParaSituacao(dto.situacaoSubprocesso) ?? undefined,
        dataLimite: dto.dataLimite ?? undefined,
        mapaCodigo: dto.mapaCodigo ?? undefined,
        localizacaoAtualCodigo: dto.localizacaoAtualCodigo ?? undefined,
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
