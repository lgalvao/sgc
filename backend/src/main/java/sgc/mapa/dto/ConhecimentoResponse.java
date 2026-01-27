package sgc.mapa.dto;

import lombok.Builder;

/**
 * Response para consultas de Conhecimento.
 */
@Builder
public record ConhecimentoResponse(
        Long codigo,
        Long atividadeCodigo,
        String descricao
) {
}
