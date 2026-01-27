package sgc.mapa.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.mapa.model.TipoImpactoAtividade;

/**
 * DTO que representa uma atividade que sofreu alteração durante a revisão do cadastro.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Getter
@Builder
@AllArgsConstructor
public class AtividadeImpactadaDto {

    /**
     * O código da atividade.
     */
    private final Long codigo;

    /**
     * A descrição atual da atividade.
     */
    private final String descricao;

    /**
     * O tipo de impacto (INSERIDA, REMOVIDA, ALTERADA).
     */
    private final TipoImpactoAtividade tipoImpacto;

    /**
     * A descrição anterior da atividade.
     */
    private final String descricaoAnterior;

    /**
     * Nomes das competências vinculadas a esta atividade.
     */
    private final List<String> competenciasVinculadas;
}
