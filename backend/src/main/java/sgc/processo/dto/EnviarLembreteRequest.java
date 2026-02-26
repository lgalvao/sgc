package sgc.processo.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Builder
public record EnviarLembreteRequest(
        @NotNull(message = "O código da unidade é obrigatório") Long unidadeCodigo) {
}
