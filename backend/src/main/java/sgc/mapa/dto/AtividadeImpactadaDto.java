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
        Long codigo,
        String descricao,
        TipoImpactoAtividade tipoImpacto,
        String descricaoAnterior,
        List<String> competenciasVinculadas) {
}
