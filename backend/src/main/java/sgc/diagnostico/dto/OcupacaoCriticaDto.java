package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OcupacaoCriticaDto(
        @NotNull Long competenciaCodigo,
        @NotBlank String servidorTitulo,
        @NotBlank String situacaoCapacitacao
) {
}
