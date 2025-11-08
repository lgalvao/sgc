/**
 * Tipos de situação de um processo.
 */
export enum SituacaoProcesso {
    CRIADO = 'CRIADO',
    FINALIZADO = 'FINALIZADO',
    EM_ANDAMENTO = 'EM_ANDAMENTO',
}

/**
 * Tipos de processo.
 */
export enum TipoProcesso {
    MAPEAMENTO = 'MAPEAMENTO',
    REVISAO = 'REVISAO',
    DIAGNOSTICO = 'DIAGNOSTICO',
}

/**
 * Perfis de usuário no sistema.
 */
export enum Perfil {
    ADMIN = 'ADMIN',
    GESTOR = 'GESTOR',
    CHEFE = 'CHEFE',
    SERVIDOR = 'SERVIDOR',
}

/**
 * Representa uma unidade organizacional.
 */
export interface UnidadeResponsavel extends Usuario {
    usuarioTitulo?: string;
    unidadeCodigo?: number;
    tipo?: string;
    idServidor?: number;
    dataInicio?: string;
    dataFim?: string | null;
}

export interface Unidade {
    codigo: number;
    nome: string;
    sigla: string;
    unidadeSuperiorCodigo?: number;
    filhas?: Unidade[];
    tipo?: string;
    idServidorTitular?: number;
    responsavel?: UnidadeResponsavel;
}

/**
 * Representa o perfil de um usuário em uma unidade.
 */
export interface PerfilUnidade {
    perfil: Perfil;
    unidade: Unidade;
    siglaUnidade: string;
}

/**
 * Tipos de situação de um subprocesso.
 */
export enum SituacaoSubprocesso {
    NAO_INICIADO = 'NAO_INICIADO',
    ATIVIDADES_REVISADAS = 'ATIVIDADES_REVISADAS',
    AGUARDANDO_HOMOLOGACAO_ATIVIDADES = 'AGUARDANDO_HOMOLOGACAO_ATIVIDADES',
    ATIVIDADES_HOMOLOGADAS = 'ATIVIDADES_HOMOLOGADAS',
    MAPEAMENTO_CONCLUIDO = 'MAPEAMENTO_CONCLUIDO',
    MAPA_VALIDADO = 'MAPA_VALIDADO',
    AGUARDANDO_AJUSTES_MAPA = 'AGUARDANDO_AJUSTES_MAPA',
    MAPA_AJUSTADO = 'MAPA_AJUSTADO',
    AGUARDANDO_HOMOLOGACAO_MAPA = 'AGUARDANDO_HOMOLOGACAO_MAPA',
    MAPA_HOMOLOGADO = 'MAPA_HOMOLOGADO',
    CONCLUIDO = 'CONCLUIDO',
    REVISAO_CADASTRO_EM_ANDAMENTO = 'REVISAO_CADASTRO_EM_ANDAMENTO',
    CADASTRO_EM_ANDAMENTO = 'CADASTRO_EM_ANDAMENTO',
}

export interface Conhecimento {
    id: number;
    descricao: string;
}

export interface Atividade {
    codigo: number;
    descricao: string;
    conhecimentos: { id: number; descricao: string; }[];
}

export interface Competencia {
    codigo: number;
    descricao: string;
    atividadesAssociadas: number[];
}

export interface Mapa {
    codigo: number;
    descricao: string;
    unidade: Unidade;
    codProcesso: number;
    competencias: Competencia[];
    situacao: string;
    dataCriacao: string;
    dataDisponibilizacao?: string;
    dataFinalizacao?: string;
}

export interface Processo {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    situacao: SituacaoProcesso;
    dataLimite: string;
    dataCriacao: string;
    dataFinalizacao?: string;
    unidades: UnidadeParticipante[];
    resumoSubprocessos: ProcessoResumo[];
}

export interface ProcessoResumo {
    codigo: number;
    descricao: string;
    situacao: SituacaoProcesso;
    tipo: TipoProcesso;
    dataLimite: string;
    dataCriacao: string;
    unidadeCodigo: number;
    unidadeNome: string;
    dataFinalizacao?: string;
    dataFinalizacaoFormatada?: string;
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

export interface Usuario {
    codigo: number;
    nome: string;
    tituloEleitoral: string;
    unidade: Unidade;
    email: string;
    ramal: string;
}

export interface AlertaFormatado {
    codigo: number;
    processo: string;
    dataHora: string;
    unidadeOrigem: Unidade;
    unidadeDestino: Unidade;
    usuarioDestino: Usuario;
    descricao: string;
    dataHoraFormatada: string;
    origem: string;
    mensagem: string;
    data: string;
}

export interface Movimentacao {
    codigo: number;
    subprocesso: Subprocesso;
    dataHora: string;
    unidadeOrigem: Unidade;
    unidadeDestino: Unidade;
    descricao: string;
    usuario: Usuario;
}

export interface AnaliseValidacao {
    codigo: number;
    dataHora: string;
    analista: string;
    unidade: string;
    acao: string;
    observacoes: string;
    resultado: string;
    codSubrocesso: number;
}

export interface AnaliseCadastro {
    codigo: number;
    dataHora: string;
    analista: string;
    unidadeSigla: string;
    acao: string;
    observacoes: string;
    resultado: string;
    codSubrocesso: number;
}

export interface AtribuicaoTemporaria {
    codigo: number;
    servidor: Usuario;
    unidade: Unidade;
    dataInicio: string;
    dataFim: string;
    dataTermino: string;
    justificativa: string;
}

export interface UnidadeSnapshot {
    codigo: number;
    nome: string;
    sigla: string;
    filhas?: UnidadeSnapshot[];
}

export interface CriarAtividadeRequest {
    descricao: string;
}

export interface CriarConhecimentoRequest {
    descricao: string;
}

export interface CriarProcessoRequest {
    descricao: string;
    tipo: TipoProcesso;
    dataLimiteEtapa1: string;
    unidades: number[];
}

export interface AtualizarProcessoRequest {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    dataLimiteEtapa1: string;
    unidades: number[];
}

export interface UnidadeParticipante {
    nome: string;
    sigla: string;
    codUnidade: number;
    codSubprocesso: number;
    codUnidadeSuperior?: number;
    situacaoSubprocesso: SituacaoSubprocesso;
    dataLimite: string;
    filhos: UnidadeParticipante[];
}

export interface ProcessoDetalhe {
    codigo: number;
    descricao: string;
    tipo: TipoProcesso;
    situacao: SituacaoProcesso;
    dataLimite: string;
    dataCriacao: string;
    dataFinalizacao?: string;
    unidades: UnidadeParticipante[];
    resumoSubprocessos: ProcessoResumo[];
    podeFinalizar: boolean;
    podeHomologarCadastro: boolean;
    podeHomologarMapa: boolean;
}

export interface ConhecimentoVisualizacao {
    id: number;
    descricao: string;
}

export interface AtividadeVisualizacao {
    codigo: number;
    descricao: string;
    conhecimentos: ConhecimentoVisualizacao[];
}

export interface CompetenciaVisualizacao {
    codigo: number;
    descricao: string;
    atividades: AtividadeVisualizacao[];
}

export interface MapaVisualizacao {
    codigo: number;
    descricao: string;
    competencias: CompetenciaVisualizacao[];
    sugestoes?: string;
}

export interface AceitarCadastroRequest {
    observacoes: string;
}

export interface DevolverCadastroRequest {
    motivo: string;
    observacoes: string;
}

export interface HomologarCadastroRequest {
    observacoes: string;
}

export interface SalvarMapaRequest {
    competencias: {
        codigo?: number;
        descricao: string;
        atividades: {
            codigo?: number;
            descricao: string;
            conhecimentos: {
                id?: number;
                descricao: string;
            }[];
        }[];
    }[];
}

export interface SalvarAjustesRequest {
    competencias: {
        codigo: number;
        descricao: string;
    }[];
    atividades: {
        codigo: number;
        descricao: string;
    }[];
    sugestoes: string;
}

export interface MapaCompleto {
    codigo: number;
    subprocessoCodigo: number;
    observacoes: string;
    competencias: Competencia[];
}

export interface MapaAjuste {
    codigo: number;
    descricao: string;
    competencias: Competencia[];
}

export interface ImpactoMapa {
    temImpactos: boolean;
    totalAtividadesInseridas: number;
    totalAtividadesRemovidas: number;
    totalAtividadesAlteradas: number;
    totalCompetenciasImpactadas: number;
    atividadesInseridas: Atividade[];
    atividadesRemovidas: Atividade[];
    atividadesAlteradas: Atividade[];
    competenciasImpactadas: Competencia[];
}

export interface DisponibilizarMapaRequest {
    dataLimite: string;
    observacoes: string;
}

export interface SubprocessoElegivel {
    codSubprocesso: number;
    unidadeNome: string;
    unidadeSigla: string;
    situacao: SituacaoSubprocesso;
}

export interface Servidor {
    codigo: number;
    nome: string;
    tituloEleitoral: string;
    unidade: Unidade;
    email: string;
    ramal: string;
}
