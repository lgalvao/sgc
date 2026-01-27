package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Builder
public record EnviarLembreteRequest(
                @NotNull(message = "O código da unidade é obrigatório") Long unidadeCodigo) {
}
