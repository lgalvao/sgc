/**
 * Tipos de situação de um processo.
 */
export enum SituacaoProcesso {
    CRIADO = "CRIADO",
    FINALIZADO = "FINALIZADO",
    EM_ANDAMENTO = "EM_ANDAMENTO",
}

/**
 * Tipos de processo.
 */
export enum TipoProcesso {
    MAPEAMENTO = "MAPEAMENTO",
    REVISAO = "REVISAO",
    DIAGNOSTICO = "DIAGNOSTICO",
}

/**
 * Perfis de usuário no sistema.
 */
export enum Perfil {
    ADMIN = "ADMIN",
    GESTOR = "GESTOR",
    CHEFE = "CHEFE",
    SERVIDOR = "SERVIDOR",
}

/**
 * Representa uma unidade organizacional.
 */
export interface Unidade {
    codigo: number;
    nome: string;
    sigla: string;
    unidadeSuperiorCodigo?: number;
    filhas?: Unidade[];
    tipo?: string;
    usuarioCodigo?: number;
    tituloTitular?: string;
    isElegivel?: boolean;
    responsavel?: Responsavel | null;
}

export interface Responsavel {
    codigo: number;
    nome: string;
    tituloEleitoral: string;
    unidade: Unidade;
    email: string;
    ramal: string;
    usuarioTitulo: string;
    unidadeCodigo: number;
    usuarioCodigo: number;
    tipo: string;
    dataInicio: string;
    dataFim: string | null;
}

/**
 * Tipos de situação de um subprocesso.
 */
export enum SituacaoSubprocesso {
    NAO_INICIADO = "NAO_INICIADO",

    // Situações de subprocessos de Mapeamento
    MAPEAMENTO_CADASTRO_EM_ANDAMENTO = "MAPEAMENTO_CADASTRO_EM_ANDAMENTO",
    MAPEAMENTO_CADASTRO_DISPONIBILIZADO = "MAPEAMENTO_CADASTRO_DISPONIBILIZADO",
    MAPEAMENTO_CADASTRO_HOMOLOGADO = "MAPEAMENTO_CADASTRO_HOMOLOGADO",
    MAPEAMENTO_MAPA_CRIADO = "MAPEAMENTO_MAPA_CRIADO",
    MAPEAMENTO_MAPA_DISPONIBILIZADO = "MAPEAMENTO_MAPA_DISPONIBILIZADO",
    MAPEAMENTO_MAPA_COM_SUGESTOES = "MAPEAMENTO_MAPA_COM_SUGESTOES",
    MAPEAMENTO_MAPA_VALIDADO = "MAPEAMENTO_MAPA_VALIDADO",
    MAPEAMENTO_MAPA_HOMOLOGADO = "MAPEAMENTO_MAPA_HOMOLOGADO",

    // Situações de subprocessos de Revisão
    REVISAO_CADASTRO_EM_ANDAMENTO = "REVISAO_CADASTRO_EM_ANDAMENTO",
    REVISAO_CADASTRO_DISPONIBILIZADA = "REVISAO_CADASTRO_DISPONIBILIZADA",
    REVISAO_CADASTRO_HOMOLOGADA = "REVISAO_CADASTRO_HOMOLOGADA",
    REVISAO_MAPA_AJUSTADO = "REVISAO_MAPA_AJUSTADO",
    REVISAO_MAPA_DISPONIBILIZADO = "REVISAO_MAPA_DISPONIBILIZADO",
    REVISAO_MAPA_COM_SUGESTOES = "REVISAO_MAPA_COM_SUGESTOES",
    REVISAO_MAPA_VALIDADO = "REVISAO_MAPA_VALIDADO",
    REVISAO_MAPA_HOMOLOGADO = "REVISAO_MAPA_HOMOLOGADO",
}

export interface Conhecimento {
    codigo: number;
    descricao: string;
}

export interface Atividade {
    codigo: number;
    descricao: string;
    conhecimentos: { codigo: number; descricao: string }[];
    mapaCodigo?: number;
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
    // Campos formatados do backend
    dataLimiteFormatada?: string;
    dataCriacaoFormatada?: string;
    dataFinalizacaoFormatada?: string;
    situacaoLabel?: string;
    tipoLabel?: string;
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
    unidadesParticipantes?: string;
    dataFinalizacao?: string;
    dataFinalizacaoFormatada?: string;
    linkDestino?: string;
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

export interface Alerta {
    codigo: number;
    codProcesso: number;
    unidadeOrigem: string;
    unidadeDestino: string;
    descricao: string;
    dataHora: string;
    dataHoraLeitura: string | null;
    mensagem: string;
    dataHoraFormatada: string;
    origem: string;
    processo: string;
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
    usuario: Usuario;
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

export interface UnidadeParticipante {
    nome: string;
    sigla: string;
    codUnidade: number;
    codSubprocesso: number;
    codUnidadeSuperior?: number;
    situacaoSubprocesso: SituacaoSubprocesso;
    dataLimite: string;
    mapaCodigo?: number;
    filhos: UnidadeParticipante[];
    // Campos formatados do backend
    dataLimiteFormatada?: string;
    situacaoLabel?: string;
}

export interface SubprocessoPermissoes {
    podeVerPagina: boolean;
    podeEditarMapa: boolean;
    podeVisualizarMapa: boolean;
    podeDisponibilizarCadastro: boolean;
    podeDevolverCadastro: boolean;
    podeAceitarCadastro: boolean;
    podeVisualizarDiagnostico: boolean;
    podeAlterarDataLimite: boolean;
    podeVisualizarImpacto: boolean;
}

export interface SubprocessoDetalhe {
    unidade: Unidade;
    titular: Usuario;
    responsavel: Usuario;
    situacao: SituacaoSubprocesso;
    situacaoLabel: string;
    localizacaoAtual: string;
    processoDescricao: string;
    tipoProcesso: TipoProcesso;
    prazoEtapaAtual: string;
    isEmAndamento: boolean;
    etapaAtual: number;
    movimentacoes: Movimentacao[];
    elementosProcesso: any[];
    permissoes: SubprocessoPermissoes;
}

export interface ConhecimentoVisualizacao {
    codigo: number;
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
    atividadesSemCompetencia?: AtividadeVisualizacao[];
    sugestoes?: string;
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

export interface SalvarMapaRequest {
    competencias: {
        codigo?: number;
        descricao: string;
        atividades: {
            codigo?: number;
            descricao: string;
            conhecimentos: {
                codigo?: number;
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
    situacao: string;
}

export interface MapaAjuste {
    codigo: number;
    descricao: string;
    competencias: Competencia[];
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



/**
 * Permissões do usuário para um subprocesso.
 */
export interface SubprocessoPermissoes {
    podeVerPagina: boolean;
    podeEditarMapa: boolean;
    podeVisualizarMapa: boolean;
    podeDisponibilizarCadastro: boolean; // Confirmação de disponibilidade
    podeDevolverCadastro: boolean;
    podeAceitarCadastro: boolean;
    podeVisualizarDiagnostico: boolean;
    podeAlterarDataLimite: boolean;
    podeVisualizarImpacto: boolean;
    podeRealizarAutoavaliacao: boolean;
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

/**
 * Status leve de um subprocesso retornado nas operações CRUD.
 */
export interface SubprocessoStatus {
    codigo: number;
    situacao: SituacaoSubprocesso;
    situacaoLabel: string;
}

/**
 * Resposta enriquecida das operações CRUD em atividades/conhecimentos.
 * Inclui a atividade afetada e o status atualizado do subprocesso.
 */
export interface AtividadeOperacaoResponse {
    atividade: Atividade | null;
    subprocesso: SubprocessoStatus;
}

/**
 * Tipos de impacto em atividades.
 */
export enum TipoImpactoAtividade {
    INSERIDA = "INSERIDA",
    REMOVIDA = "REMOVIDA",
    ALTERADA = "ALTERADA",
}

/**
 * Tipos de impacto em competências.
 */
export enum TipoImpactoCompetencia {
    ATIVIDADE_REMOVIDA = "ATIVIDADE_REMOVIDA",
    ATIVIDADE_ALTERADA = "ATIVIDADE_ALTERADA",
    IMPACTO_GENERICO = "IMPACTO_GENERICO",
}

export interface AtividadeImpactada {
    codigo: number;
    descricao: string;
    tipoImpacto: TipoImpactoAtividade;
    descricaoAnterior?: string;
    competenciasVinculadas: string[];
}

export interface CompetenciaImpactada {
    codigo: number;
    descricao: string;
    atividadesAfetadas: string[];
    tipoImpacto: TipoImpactoCompetencia;
}

export interface ImpactoMapa {
    temImpactos: boolean;
    totalAtividadesInseridas: number;
    totalAtividadesRemovidas: number;
    totalAtividadesAlteradas: number;
    totalCompetenciasImpactadas: number;
    atividadesInseridas: AtividadeImpactada[];
    atividadesRemovidas: AtividadeImpactada[];
    atividadesAlteradas: AtividadeImpactada[];
    competenciasImpactadas: CompetenciaImpactada[];
}
