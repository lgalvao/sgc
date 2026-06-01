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
    codUnidadeSuperior: number | null;
    sigla: string;
    nome: string;
    codSubprocesso: number | null;
    situacaoSubprocesso: string | null;
    dataLimite: string | null;
    mapaCodigo: number | null;
    localizacaoAtualCodigo: number | null;
    filhos: UnidadeParticipanteDto[];
}

export interface ProcessoDetalheResponseBackend {
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
