package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para requisição de reabertura de processo (CDU-05 item 15).
 */
@Builder
public record ReabrirProcessoRequest(
                /**
                 * Justificativa para a reabertura do processo.
                 */
                @Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres") String justificativa) {
}
