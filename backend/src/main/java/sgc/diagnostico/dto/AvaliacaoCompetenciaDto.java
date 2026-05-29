package sgc.diagnostico.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AvaliacaoCompetenciaDto(
        @NotNull Long competenciaCodigo,
        @Min(0) @Max(6) Integer importancia,
        @Min(0) @Max(6) Integer dominio
) {
}

