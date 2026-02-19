import type { Analise } from "@/types/tipos";

/**
 * Mapeia um DTO de histórico de análise vindo do backend para o modelo do frontend.
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

export function mapAnalisesArray(dtos: any[]): Analise[] {
    if (!dtos) return [];
    return dtos.map(mapAnaliseDtoToModel);
}
