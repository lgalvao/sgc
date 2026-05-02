import type {TipoProcesso} from "./comum";
import type {Atividade, MapaCompleto, MapaResumo} from "./mapa";
import type {Usuario, Unidade} from "./organizacao";

export enum SituacaoSubprocesso {
  NAO_INICIADO = "NAO_INICIADO",
  MAPEAMENTO_CADASTRO_EM_ANDAMENTO = "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
  MAPEAMENTO_CADASTRO_DISPONIBILIZADO = "MAPEAMENTO_CADASTRO_DISPONIBILIZADO",
  MAPEAMENTO_CADASTRO_HOMOLOGADO = "MAPEAMENTO_CADASTRO_HOMOLOGADO",
  MAPEAMENTO_MAPA_CRIADO = "MAPEAMENTO_MAPA_CRIADO",
  MAPEAMENTO_MAPA_DISPONIBILIZADO = "MAPEAMENTO_MAPA_DISPONIBILIZADO",
  MAPEAMENTO_MAPA_COM_SUGESTOES = "MAPEAMENTO_MAPA_COM_SUGESTOES",
  MAPEAMENTO_MAPA_VALIDADO = "MAPEAMENTO_MAPA_VALIDADO",
  MAPEAMENTO_MAPA_HOMOLOGADO = "MAPEAMENTO_MAPA_HOMOLOGADO",
  REVISAO_CADASTRO_EM_ANDAMENTO = "REVISAO_CADASTRO_EM_ANDAMENTO",
  REVISAO_CADASTRO_DISPONIBILIZADA = "REVISAO_CADASTRO_DISPONIBILIZADA",
  REVISAO_CADASTRO_HOMOLOGADA = "REVISAO_CADASTRO_HOMOLOGADA",
  REVISAO_MAPA_AJUSTADO = "REVISAO_MAPA_AJUSTADO",
  REVISAO_MAPA_DISPONIBILIZADO = "REVISAO_MAPA_DISPONIBILIZADO",
  REVISAO_MAPA_COM_SUGESTOES = "REVISAO_MAPA_COM_SUGESTOES",
  REVISAO_MAPA_VALIDADO = "REVISAO_MAPA_VALIDADO",
  REVISAO_MAPA_HOMOLOGADO = "REVISAO_MAPA_HOMOLOGADO",
}

export interface Subprocesso {
  codigo: number;
  unidade: Unidade;
  situacao: SituacaoSubprocesso;
  dataLimite: string;
  dataFimEtapa1: string;
  dataLimiteEtapa2: string;
  atividades: Atividade[];
  codUnidade: number;
}

export interface Movimentacao {
  codigo: number;
  dataHora: string;
  unidadeOrigemCodigo: number;
  unidadeOrigemSigla: string;
  unidadeOrigemNome: string;
  unidadeDestinoCodigo: number;
  unidadeDestinoSigla: string;
  unidadeDestinoNome: string;
  usuarioTitulo: string;
  usuarioNome: string;
  descricao: string;
}

export interface Analise {
  dataHora: string;
  observacoes: string;
  acao: string;
  unidadeSigla: string;
  unidadeNome: string;
  analistaUsuarioTitulo: string;
  motivo: string;
  tipo: string;
}

export interface UnidadeParticipante {
  nome: string;
  sigla: string;
  codUnidade: number;
  codSubprocesso: number;
  codUnidadeSuperior?: number;
  situacaoSubprocesso: SituacaoSubprocesso;
  localizacaoAtualCodigo?: number;
  dataLimite: string;
  mapaCodigo?: number;
  filhos: UnidadeParticipante[];
}

export interface UnidadeImportacao {
  nome: string;
  sigla: string;
  codUnidade: number;
  codSubprocesso: number;
  codUnidadeSuperior?: number;
  situacaoSubprocesso?: SituacaoSubprocesso;
  localizacaoAtualCodigo?: number;
  dataLimite?: string;
  mapaCodigo?: number;
}

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

export interface ResponsavelDto {
  usuario: Usuario;
  tipo: string;
  dataInicio: string;
  dataFim: string | null;
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
  permissoes: PermissoesSubprocesso;
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

export interface AceitarCadastroRequest {
  observacoes: string;
}

export interface DevolverCadastroRequest {
  observacoes: string;
}

export interface HomologarCadastroRequest {
  observacoes: string;
}

export interface SubprocessoElegivel {
  codigo: number;
  unidadeCodigo: number;
  unidadeNome: string;
  unidadeSigla: string;
  localizacaoCodigo: number;
  situacao: SituacaoSubprocesso;
  habilitarAceitarCadastroBloco: boolean;
  habilitarAceitarMapaBloco: boolean;
  habilitarHomologarCadastroBloco: boolean;
  habilitarHomologarMapaBloco: boolean;
  habilitarDisponibilizarMapaBloco: boolean;
  ultimaDataLimite?: string;
}

export interface ErroValidacao {
  tipo: string;
  atividadeCodigo?: number;
  descricaoAtividade?: string;
  mensagem: string;
}

export interface ValidacaoCadastro {
  valido: boolean;
  erros: ErroValidacao[];
}

export interface UnidadeSelecao {
  codigo: number;
  sigla: string;
  nome: string;
  situacao: string;
  ultimaDataLimite?: string;
}

export interface SubprocessoStatus {
  codigo: number;
  situacao: SituacaoSubprocesso;
}

export interface AtividadeOperacaoResponse {
  atividade: Atividade | null;
  subprocesso: SubprocessoStatus;
  atividadesAtualizadas: Atividade[];
  permissoes: PermissoesSubprocesso;
  message?: string | null;
  aviso?: string | null;
}

export interface RespostaLocalCadastro {
  subprocesso: SubprocessoStatus;
  permissoes: PermissoesSubprocesso;
  atividadesAtualizadas: Atividade[];
}
