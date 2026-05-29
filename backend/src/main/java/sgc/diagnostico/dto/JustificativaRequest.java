package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotBlank;

public record JustificativaRequest(
        @NotBlank String justificativa
) {
}
