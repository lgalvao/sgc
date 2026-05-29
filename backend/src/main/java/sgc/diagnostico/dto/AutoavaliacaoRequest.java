package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AutoavaliacaoRequest(@NotEmpty List<AvaliacaoCompetenciaDto> competencias) {
}