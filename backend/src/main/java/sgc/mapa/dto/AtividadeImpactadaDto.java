package sgc.mapa.dto;

import lombok.Builder;
import sgc.mapa.model.TipoImpactoAtividade;

import java.util.List;

/**
 * DTO que representa uma atividade que sofreu alteração durante a revisão do
 * cadastro.
 *
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
@Builder
public record AtividadeImpactadaDto(
                /**
                 * O código da atividade.
                 */
                Long codigo,

                /**
                 * A descrição atual da atividade.
                 */
                String descricao,

                /**
                 * O tipo de impacto (INSERIDA, REMOVIDA, ALTERADA).
                 */
                TipoImpactoAtividade tipoImpacto,

                /**
                 * A descrição anterior da atividade.
                 */
                String descricaoAnterior,

                /**
                 * Nomes das competências vinculadas a esta atividade.
                 */
                List<String> competenciasVinculadas) {
}
