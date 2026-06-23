package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public record ConsensoRequest(
        @NotEmpty List<ConsensoCompetenciaDto> competencias
) {
}
