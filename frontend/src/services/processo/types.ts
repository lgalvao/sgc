import {
    type AcaoBlocoProcesso,
    type AtualizarProcessoRequest,
    type CriarProcessoRequest,
    type Processo,
    type ProcessoResumo,
    SituacaoProcesso,
    TipoProcesso,
    type UnidadeImportacao
} from "@/types/tipos";

export type {
    AcaoBlocoProcesso,
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    UnidadeImportacao
};

export interface UnidadeParticipanteDto {
    codUnidade: number;
    codUnidadeSuperior?: number;
    sigla?: string;
    nome?: string;
    codSubprocesso?: number;
    situacaoSubprocesso?: string;
    dataLimite?: string;
    mapaCodigo?: number;
    localizacaoAtualCodigo?: number;
    filhos?: UnidadeParticipanteDto[];
}

export interface ProcessoDetalheDto {
    codigo?: number;
    tipo?: string;
    situacao?: string;
    dataCriacao?: string;
    dataInicio?: string;
    dataFinalizacao?: string;
    unidades?: UnidadeParticipanteDto[];
    acoesBloco?: unknown[];

    [key: string]: unknown; // Para campos adicionais do spread
}

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
