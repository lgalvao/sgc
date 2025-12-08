package sgc.diagnostico.dto;

import lombok.Builder;

/**
 * DTO de resposta para avaliação de competência por servidor.
 */
@Builder
public record AvaliacaoServidorDto(
        Long codigo,
        Long competenciaCodigo,
        String competenciaDescricao,
        String importancia,
        String importanciaLabel,
        String dominio,
        String dominioLabel,
        Integer gap,
        String observacoes
) {
}
