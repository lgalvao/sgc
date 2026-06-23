package sgc.mapa.dto;

import lombok.*;

import java.util.*;

/**
 * DTO que representa uma atividade que sofreu alteração durante a revisão do cadastro.
 */
@Builder
public record AtividadeImpactadaDto(
        Long codigo,
        String descricao,
        String tipoImpacto,
        String descricaoAnterior,
        List<String> conhecimentos,
        List<String> conhecimentosAdicionados,
        List<String> conhecimentosRemovidos,
        List<String> competenciasVinculadas) {
}
