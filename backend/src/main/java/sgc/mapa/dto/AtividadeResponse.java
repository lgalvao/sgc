package sgc.mapa.dto;

import lombok.Builder;

/**
 * Response para consultas de Atividade.
 */
@Builder
public record AtividadeResponse(
        Long codigo,
        Long mapaCodigo,
        String descricao
) {
}
