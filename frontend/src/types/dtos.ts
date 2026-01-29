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
