package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record SituacaoCapacitacaoDto(
        @NotNull Long competenciaCodigo,
        @NotBlank String servidorTitulo,
        @Nullable String servidorNome,
        @Nullable String situacaoCapacitacao
) {
}
