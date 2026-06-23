package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;

public record JustificativaRequest(
        @NotBlank String justificativa
) {
}
