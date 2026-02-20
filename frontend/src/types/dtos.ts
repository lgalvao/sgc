/**
 * Tipos de DTOs retornados pelo backend.
 * Esses tipos são usados para fornecer type safety aos mappers.
 */

import type {SituacaoSubprocesso, TipoImpactoAtividade} from './tipos';

export interface AtividadeDto {
    codigo: number;
    descricao: string;
    conhecimentos?: ConhecimentoDto[];
}

export interface ConhecimentoDto {
    codigo: number;
    descricao: string;
}

export interface AtividadeImpactadaDto {
    codigo: number;
    descricao: string;
    tipoImpacto: TipoImpactoAtividade;
    descricaoAnterior?: string;
    competenciasVinculadas: string[];
}

export interface CompetenciaImpactadaDto {
    codigo: number;
    descricao: string;
    atividadesAfetadas?: string[];
    tiposImpacto: string[];
}

export interface ImpactoMapaDto {
    temImpactos: boolean;
    inseridas?: AtividadeImpactadaDto[];
    removidas?: AtividadeImpactadaDto[];
    alteradas?: AtividadeImpactadaDto[];
    competenciasImpactadas?: CompetenciaImpactadaDto[];
}

export interface AlertaDto {
    codigo: number;
    codProcesso: number;
    descricao: string;
    dataHora: string;
    unidadeOrigem: string;
    unidadeDestino: string;
    dataHoraLeitura?: string;
    mensagem: string;
    origem: string;
    processo: string;
}

export interface UnidadeParticipanteDto {
    codUnidade: number;
    codUnidadeSuperior?: number;
    sigla?: string;
    nome?: string;
    codSubprocesso?: number;
    situacaoSubprocesso?: string;
    dataLimite?: string;
    mapaCodigo?: number;
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
    [key: string]: any; // Para campos adicionais do spread
}

// DTOs para SGRH (autenticação e login)
export interface UnidadeDto {
    codigo: number;
    nome: string;
    sigla: string;
}

export interface PerfilUnidadeDto {
    perfil: string;
    unidade: UnidadeDto;
    siglaUnidade: string;
}

export interface UsuarioDto {
    tituloEleitoral: string;
    nome: string;
    email: string;
    ramal: string;
    unidade: UnidadeDto;
    perfis: string[];
}

export interface LoginResponseDto {
    tituloEleitoral: string;
    nome: string;
    perfil: string;
    unidadeCodigo: number;
    token: string;
}

/**
 * DTO que representa o status atual de um subprocesso.
 * Usado para retornar informações básicas de status sem precisar carregar o processo completo.
 */
export interface SubprocessoSituacaoDto {
    codigo: number;
    situacao: SituacaoSubprocesso;
}

/**
 * DTO de resposta para operações CRUD em atividades.
 * Retorna a atividade afetada, o status atualizado do subprocesso,
 * e a lista completa de atividades atualizadas.
 */
export interface AtividadeOperacaoResponseDto {
    atividade: AtividadeDto | null;
    subprocesso: SubprocessoSituacaoDto;
    atividadesAtualizadas: AtividadeDto[];
}


