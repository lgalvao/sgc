package sgc.processo.dto;

import lombok.*;

import java.time.*;

/**
 * DTO de resposta com resumo de um processo.
 */
@Builder
public record ProcessoResumoDto(
        Long codigo,
        String descricao,
        String situacao,
        String tipo,
        LocalDateTime dataLimite,
        LocalDateTime dataCriacao,
        LocalDateTime dataFinalizacao,
        Long unidadeCodigo,
        String unidadeNome,
        String unidadesParticipantes,
        String linkDestino
) {
}
