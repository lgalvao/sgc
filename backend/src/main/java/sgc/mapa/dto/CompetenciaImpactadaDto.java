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
                /**
                 * O código da competência.
                 */
                Long codigo,

                /**
                 * A descrição da competência.
                 */
                String descricao,

                /**
                 * Lista com as descrições das atividades que causaram o impacto.
                 */
                List<String> atividadesAfetadas,

                /**
                 * Os tipos de impacto sofridos pela competência.
                 * Uma competência pode ser afetada por múltiplos tipos de mudança (ex:
                 * atividade removida E alterada).
                 */
                List<TipoImpactoCompetencia> tiposImpacto) {
}
