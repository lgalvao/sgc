package sgc.mapa.dto;

import lombok.*;

import java.util.*;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        Long codigo,
        String descricao,
        List<ConhecimentoResumoDto> conhecimentos) {
}
