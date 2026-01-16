package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Getter
@Builder
@AllArgsConstructor
public class EnviarLembreteRequest {
    @NotNull(message = "O código da unidade é obrigatório")
    private final Long unidadeCodigo;
}
