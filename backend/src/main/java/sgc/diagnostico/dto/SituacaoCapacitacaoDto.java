package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record SituacaoCapacitacaoDto(
        @NotNull Long competenciaCodigo,
        @NotBlank String servidorTitulo,
        @Nullable String servidorNome,
        @Nullable String situacaoCapacitacao
) {
}
