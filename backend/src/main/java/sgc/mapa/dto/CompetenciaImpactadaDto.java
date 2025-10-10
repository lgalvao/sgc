package sgc.mapa.dto;

import sgc.mapa.enums.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças
 * nas atividades durante a revisão do cadastro.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
public record CompetenciaImpactadaDto(
    Long codigo,
    String descricao,
    List<String> atividadesAfetadas,  // Descrições das atividades que causaram impacto
    TipoImpactoCompetencia tipoImpacto  // NOVA_ATIVIDADE, ATIVIDADE_REMOVIDA, ATIVIDADE_ALTERADA
) {}