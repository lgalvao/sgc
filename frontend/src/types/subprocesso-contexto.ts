import type {TipoProcesso} from "./comum";
import type {Atividade, MapaCompleto, MapaResumo} from "./mapa";
import type {Unidade, Usuario} from "./organizacao";
import type {Movimentacao, ResponsavelDto, SituacaoSubprocesso, Subprocesso} from "./subprocesso-modelos";

export interface PermissoesSubprocesso {
    podeEditarCadastro: boolean;
    podeDisponibilizarCadastro: boolean;
    podeDevolverCadastro: boolean;
    podeAceitarCadastro: boolean;
    podeHomologarCadastro: boolean;
    podeEditarMapa: boolean;
    podeDisponibilizarMapa: boolean;
    podeValidarMapa: boolean;
    podeApresentarSugestoes: boolean;
    podeVerSugestoes: boolean;
    podeDevolverMapa: boolean;
    podeAceitarMapa: boolean;
    podeHomologarMapa: boolean;
    podeVisualizarImpacto: boolean;
    podeAlterarDataLimite: boolean;
    podeReabrirCadastro: boolean;
    podeReabrirRevisao: boolean;
    podeEnviarLembrete: boolean;
    mostrarExportacaoMapa: boolean;
    mostrarHistoricoAnaliseDiagnostico: boolean;
    podePreencherAutoavaliacao?: boolean;
    podeCriarConsenso?: boolean;
    podeConcluirDiagnostico?: boolean;
    podeValidarDiagnostico?: boolean;
    podeDevolverDiagnostico?: boolean;
    podeHomologarDiagnostico?: boolean;
    mesmaUnidade: boolean;
    habilitarAcessoCadastro: boolean;
    habilitarAcessoMapa: boolean;
    habilitarAcessoDiagnostico?: boolean;
    habilitarEditarCadastro: boolean;
    habilitarDisponibilizarCadastro: boolean;
    habilitarDevolverCadastro: boolean;
    habilitarAceitarCadastro: boolean;
    habilitarHomologarCadastro: boolean;
    habilitarEditarMapa: boolean;
    habilitarDisponibilizarMapa: boolean;
    habilitarValidarMapa: boolean;
    habilitarApresentarSugestoes: boolean;
    habilitarDevolverMapa: boolean;
    habilitarAceitarMapa: boolean;
    habilitarHomologarMapa: boolean;
    habilitarAlterarDataLimite: boolean;
    habilitarReabrirCadastro: boolean;
    habilitarReabrirRevisao: boolean;
    habilitarEnviarLembrete: boolean;
    habilitarPreencherAutoavaliacao?: boolean;
    habilitarCardConsenso?: boolean;
    habilitarCardSituacaoCapacitacao?: boolean;
    habilitarCriarConsenso?: boolean;
    habilitarConcluirDiagnostico?: boolean;
    habilitarValidarDiagnostico?: boolean;
    habilitarDevolverDiagnostico?: boolean;
    habilitarHomologarDiagnostico?: boolean;
}

export interface SubprocessoDetalhe {
    codigo: number;
    unidade: Unidade;
    titular: Usuario | null;
    responsavel: ResponsavelDto | null;
    situacao: SituacaoSubprocesso;
    localizacaoAtual: string;
    processoDescricao: string;
    dataCriacaoProcesso: string;
    dataFimEtapa1: string | null;
    ultimaDataLimiteSubprocesso: string;
    tipoProcesso: TipoProcesso;
    prazoEtapaAtual: string;
    isEmAndamento: boolean;
    etapaAtual: number | null;
    movimentacoes: Movimentacao[];
    elementosProcesso: unknown[];
    permissoes: PermissoesSubprocesso;
}

export interface SubprocessoDetalheResponse {
    subprocesso: {
        codigo: number;
        unidade: Unidade;
        situacao: SituacaoSubprocesso;
        dataLimiteEtapa1: string;
        dataFimEtapa1: string | null;
        dataLimiteEtapa2: string | null;
        dataFimEtapa2: string | null;
        ultimaDataLimite: string;
        processoDescricao: string;
        dataCriacaoProcesso: string;
        tipoProcesso: TipoProcesso;
        isEmAndamento: boolean;
        etapaAtual: number | null;
    };
    responsavel: ResponsavelDto | null;
    titular: Usuario | null;
    movimentacoes: Movimentacao[];
    localizacaoAtual: string;
    permissoes: Partial<PermissoesSubprocesso> | null;
}

export interface ContextoEdicaoSubprocesso {
    unidade: Unidade;
    subprocesso: Subprocesso;
    detalhes: SubprocessoDetalhe;
    mapa: MapaCompleto;
}

export interface ContextoCadastroAtividadesSubprocesso {
    unidade: Unidade;
    detalhes: SubprocessoDetalhe;
    mapa: MapaResumo;
    atividadesDisponiveis: Atividade[];
    assinaturaCadastroReferencia?: string;
}

type ContextoComDetalhes<TContexto, TDetalhes> = Omit<TContexto, "detalhes"> & {
    detalhes: TDetalhes;
};

export type ContextoEdicaoSubprocessoResponse =
    ContextoComDetalhes<ContextoEdicaoSubprocesso, SubprocessoDetalheResponse>;

export type ContextoCadastroAtividadesSubprocessoResponse =
    ContextoComDetalhes<ContextoCadastroAtividadesSubprocesso, SubprocessoDetalheResponse>;
