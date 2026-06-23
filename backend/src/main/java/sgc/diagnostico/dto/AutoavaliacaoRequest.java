package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public record AutoavaliacaoRequest(@NotEmpty List<AvaliacaoCompetenciaDto> competencias) {
}