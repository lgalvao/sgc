package sgc.processo.dto;

import lombok.*;
import org.jspecify.annotations.*;

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
        @Nullable LocalDateTime dataFinalizacao,
        @Nullable Long unidadeCodigo,
        @Nullable String unidadeNome,
        @Nullable String unidadesParticipantes,
        @Nullable String linkDestino
) {
}
