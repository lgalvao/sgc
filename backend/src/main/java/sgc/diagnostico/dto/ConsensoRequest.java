package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import org.jspecify.annotations.Nullable;

public record ConsensoRequest(
        @NotEmpty List<AvaliacaoCompetenciaDto> competencias,
        @Nullable List<ConsensoCompetenciaDto> competenciasDetalhadas
) {
}
