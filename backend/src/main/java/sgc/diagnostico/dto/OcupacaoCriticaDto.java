package sgc.diagnostico.dto;

/**
 * DTO de resposta para ocupação crítica.
 */
public record OcupacaoCriticaDto(
        Long codigo,
        Long competenciaCodigo,
        String competenciaDescricao,
        String situacao,
        String situacaoLabel
) {
}
