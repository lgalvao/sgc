package sgc.mapa.dto;

import sgc.mapa.modelo.TipoImpactoAtividade;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO que representa uma atividade que sofreu alteração durante
 * a revisão do cadastro.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 *
 * @param codigo                 O código da atividade.
 * @param descricao              A descrição atual da atividade.
 * @param tipoImpacto            O tipo de impacto (INSERIDA, REMOVIDA, ALTERADA).
 * @param descricaoAnterior      A descrição anterior da atividade (apenas para tipo ALTERADA).
 * @param competenciasVinculadas Nomes das competências vinculadas a esta atividade.
 */
public record AtividadeImpactadaDto(
    Long codigo,
    String descricao,
    TipoImpactoAtividade tipoImpacto,  // INSERIDA, REMOVIDA, ALTERADA
    String descricaoAnterior,  // Para ALTERADA (null para outros tipos)
    List<String> competenciasVinculadas  // Nomes das competências vinculadas
) {
    public AtividadeImpactadaDto {
        competenciasVinculadas = new ArrayList<>(competenciasVinculadas);
    }
}