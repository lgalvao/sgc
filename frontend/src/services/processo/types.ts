import {
    type AcaoBlocoProcesso,
    type AtualizarProcessoRequest,
    type CriarProcessoRequest,
    type Processo,
    type ProcessoResumo,
    SituacaoProcesso,
    type Subprocesso,
    type SubprocessoElegivel,
    TipoProcesso,
    type UnidadeImportacao,
    type UnidadeParticipante
} from "@/types/tipos";
import type {ProcessoDetalheDto, UnidadeParticipanteDto} from "@/types/dtos";

export type {
    AcaoBlocoProcesso,
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    Subprocesso,
    SubprocessoElegivel,
    UnidadeImportacao,
    UnidadeParticipante
};

export interface ProcessoDetalheResponseBackend extends ProcessoDetalheDto {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    situacao: SituacaoProcesso;
    dataLimite: string;
    dataCriacao: string;
    dataFinalizacao?: string;
    podeFinalizar: boolean;
    podeHomologarCadastro: boolean;
    podeHomologarMapa: boolean;
    podeAceitarCadastroBloco: boolean;
    podeDisponibilizarMapaBloco: boolean;
    unidades: UnidadeParticipanteDto[];
    resumoSubprocessos: ProcessoResumo[];
    acoesBloco: AcaoBlocoProcesso[];
}
