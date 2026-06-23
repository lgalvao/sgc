package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Builder
public record AvaliacaoCompetenciaDto(
        @NotNull Long competenciaCodigo,
        String competenciaDescricao,
        @Min(0) @Max(6) Integer importancia,
        @Min(0) @Max(6) Integer dominio
) {
}
