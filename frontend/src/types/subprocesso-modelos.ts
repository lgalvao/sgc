import type {Atividade} from "./mapa";
import type {Unidade, Usuario} from "./organizacao";

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
    DIAGNOSTICO_EM_ANDAMENTO = "DIAGNOSTICO_EM_ANDAMENTO",
    DIAGNOSTICO_CONCLUIDO = "DIAGNOSTICO_CONCLUIDO",
    DIAGNOSTICO_HOMOLOGADO = "DIAGNOSTICO_HOMOLOGADO",
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
    acaoDescricao: string;
    unidadeSigla: string;
    unidadeNome: string;
    analistaUsuarioTitulo: string;
    usuarioNome: string;
    motivo: string;
    tipo: string;
}

export interface UnidadeParticipante {
    nome: string;
    sigla: string;
    codUnidade: number;
    codSubprocesso: number | null;
    codUnidadeSuperior?: number;
    situacaoSubprocesso: SituacaoSubprocesso | null;
    localizacaoAtualCodigo?: number;
    dataLimite: string;
    mapaCodigo?: number;
    filhos: UnidadeParticipante[];
}

export interface UnidadeImportacao {
    nome: string;
    sigla: string;
    codUnidade: number;
    codSubprocesso: number | null;
    codUnidadeSuperior?: number;
    situacaoSubprocesso?: SituacaoSubprocesso;
    localizacaoAtualCodigo?: number;
    dataLimite?: string;
    mapaCodigo?: number;
}

export interface ResponsavelDto {
    usuario: Usuario;
    tipo: string;
    dataInicio: string;
    dataFim: string | null;
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
    habilitarAceitarDiagnosticoBloco: boolean;
    habilitarHomologarCadastroBloco: boolean;
    habilitarHomologarMapaBloco: boolean;
    habilitarDisponibilizarMapaBloco: boolean;
    ultimaDataLimite?: string;
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
