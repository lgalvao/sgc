package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ConsensoRequest(
        @NotEmpty List<AvaliacaoCompetenciaDto> competencias,
        String motivoReabertura
) {
}
