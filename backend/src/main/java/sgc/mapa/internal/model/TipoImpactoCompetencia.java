package sgc.mapa.internal.model;

/**
 * Tipos de impacto que uma competência pode sofrer em relação a mudanças em atividades.
 *
 * <p>Valores suportados: - ATIVIDADE_REMOVIDA: Uma atividade vinculada foi removida -
 * ATIVIDADE_ALTERADA: Uma atividade vinculada teve sua descrição ou conhecimentos alterados -
 * IMPACTO_GENERICO: Múltiplos tipos de impacto afetaram a competência
 */
public enum TipoImpactoCompetencia {
    ATIVIDADE_REMOVIDA,
    ATIVIDADE_ALTERADA,
    IMPACTO_GENERICO
}
