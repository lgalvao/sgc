import type { Analise } from "@/types/tipos";

/**
 * Mapeia um DTO de histórico de análise vindo do backend para o modelo do frontend.
 *
 * @param dto O DTO de histórico de análise (AnaliseHistoricoDto no backend)
 * @returns O modelo de Analise tipado
 */
export function mapAnaliseDtoToModel(dto: any): Analise {
    return {
        dataHora: dto.dataHora,
        observacoes: dto.observacoes || "",
        acao: dto.acao,
        unidadeSigla: dto.unidadeSigla,
        unidadeNome: dto.unidadeNome,
        analistaUsuarioTitulo: dto.analistaUsuarioTitulo,
        motivo: dto.motivo || "",
        tipo: dto.tipo
    };
}

/**
 * Mapeia um array de DTOs de análise.
 *
 * @param dtos Array de DTOs vindo da API
 * @returns Array de modelos Analise
 */
export function mapAnalisesArray(dtos: any[]): Analise[] {
    if (!dtos) return [];
    return dtos.map(mapAnaliseDtoToModel);
}
