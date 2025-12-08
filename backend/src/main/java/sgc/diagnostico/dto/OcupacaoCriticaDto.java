package sgc.diagnostico.dto;

import lombok.Builder;

/**
 * DTO de resposta para ocupação crítica.
 */
@Builder
public record OcupacaoCriticaDto(
        Long codigo,
        Long competenciaCodigo,
        String competenciaDescricao,
        String situacao,
        String situacaoLabel
) {
}
