package sgc.mapa.dto;

import lombok.Builder;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças nas
 * atividades durante a
 * revisão do cadastro.
 *
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
@Builder
public record CompetenciaImpactadaDto(
        Long codigo,

                String descricao,

                List<String> atividadesAfetadas,

                List<TipoImpactoCompetencia> tiposImpacto) {
}
