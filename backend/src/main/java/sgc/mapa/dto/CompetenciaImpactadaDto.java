package sgc.mapa.dto;

import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças
 * nas atividades durante a revisão do cadastro.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 *
 * @param codigo             O código da competência.
 * @param descricao          A descrição da competência.
 * @param atividadesAfetadas Lista com as descrições das atividades que causaram o impacto.
 * @param tipoImpacto        O tipo de impacto sofrido pela competência (e.g., nova atividade associada).
 */
public record CompetenciaImpactadaDto(
        Long codigo,
        String descricao,
        List<String> atividadesAfetadas,  // Descrições das atividades que causaram impacto
        TipoImpactoCompetencia tipoImpacto  // NOVA_ATIVIDADE, ATIVIDADE_REMOVIDA, ATIVIDADE_ALTERADA
) {
}