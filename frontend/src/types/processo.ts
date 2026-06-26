import type {SituacaoProcesso, TipoProcesso} from "./comum";
import type {MapaVigenteReferencia, Unidade} from "./organizacao";
import type {SubprocessoElegivel, UnidadeParticipante} from "./subprocesso";

export interface Processo {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    situacao: SituacaoProcesso;
    dataLimite: string;
    dataCriacao: string;
    dataFinalizacao?: string;
    podeFinalizar?: boolean;
    mensagemFinalizacao?: string;
    podeHomologarCadastro?: boolean;
    podeHomologarMapa?: boolean;
    podeAceitarCadastroBloco?: boolean;
    podeDisponibilizarMapaBloco?: boolean;
    unidades: UnidadeParticipante[];
    resumoSubprocessos: ProcessoResumo[];
    acoesBloco?: AcaoBlocoProcesso[];
}

export interface ProcessoResumo {
    codigo: number;
    descricao: string;
    situacao: SituacaoProcesso;
    tipo: TipoProcesso;
    dataLimite: string;
    dataCriacao: string;
    dataFinalizacao?: string;
    linkDestino?: string;
    unidadeCodigo: number;
    unidadeNome: string;
    unidadesParticipantes?: string;
}

export interface CriarProcessoRequest {
    descricao: string;
    tipo: TipoProcesso;
    dataLimiteEtapa1: string | null;
    unidades: number[];
}

export interface AtualizarProcessoRequest {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimiteEtapa1: string | null;
    unidades: number[];
}

export interface Alerta {
    codigo: number;
    codProcesso: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    descricao: string;
    dataHora: string;
    dataHoraLeitura: string | null;
    mensagem: string;
    origem: string;
    processo: string;
}

export type AcaoProcessoBloco = "ACEITAR" | "HOMOLOGAR" | "DISPONIBILIZAR";

export interface AcaoBlocoProcesso {
    codigo: string;
    acao: AcaoProcessoBloco;
    mostrar: boolean;
    habilitar: boolean;
    requerDataLimite: boolean;
    redirecionarPainel: boolean;
    rotulo: string;
    titulo: string;
    texto: string;
    rotuloBotao: string;
    mensagemSucesso: string;
    unidades: SubprocessoElegivel[];
}

export interface PainelBootstrap {
    processos: ProcessoResumo[];
    alertas: Alerta[];
}

export type {MapaVigenteReferencia, Unidade};
