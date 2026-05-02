import type {TipoProcesso} from "./comum";
import type {Atividade, MapaCompleto, MapaResumo} from "./mapa";
import type {Usuario, Unidade} from "./organizacao";
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
  mesmaUnidade: boolean;
  habilitarAcessoCadastro: boolean;
  habilitarAcessoMapa: boolean;
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
