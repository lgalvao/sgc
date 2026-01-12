package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.mapa.model.TipoImpactoAtividade;

import java.util.List;

/**
 * DTO que representa uma atividade que sofreu alteração durante a revisão do cadastro.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeImpactadaDto {

    /**
     * O código da atividade.
     */
    private Long codigo;

    /**
     * A descrição atual da atividade.
     */
    private String descricao;

    /**
     * O tipo de impacto (INSERIDA, REMOVIDA, ALTERADA).
     */
    private TipoImpactoAtividade tipoImpacto;

    /**
     * A descrição anterior da atividade (apenas para tipo ALTERADA).
     */
    private String descricaoAnterior;

    /**
     * Nomes das competências vinculadas a esta atividade.
     */
    private List<String> competenciasVinculadas;
}
