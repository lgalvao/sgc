/**
 * Tipos de DTOs retornados pelo backend.
 * Esses tipos são usados para fornecer type safety aos mappers.
 */

import type { TipoImpactoAtividade } from './tipos';

export interface AtividadeDto {
    codigo: number;
    descricao: string;
    conhecimentos?: ConhecimentoDto[];
    mapaCodigo?: number;
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
    atividadesAfetadas?: number[];
    tipoImpacto: string[];
}

export interface ImpactoMapaDto {
    temImpactos: boolean;
    totalAtividadesInseridas: number;
    totalAtividadesRemovidas: number;
    totalAtividadesAlteradas: number;
    totalCompetenciasImpactadas: number;
    atividadesInseridas?: AtividadeImpactadaDto[];
    atividadesRemovidas?: AtividadeImpactadaDto[];
    atividadesAlteradas?: AtividadeImpactadaDto[];
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
    dataHoraFormatada: string;
    origem: string;
    processo: string;
}

export interface UnidadeParticipanteDto {
    codigo: number;
    sigla?: string;
    nome?: string;
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

