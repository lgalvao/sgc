package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

/**
 * Request para salvar os ajustes feitos no mapa de competências.
 */
@Builder
public record SalvarAjustesRequest(
        @NotEmpty(message = "A lista de competências não pode ser vazia") 
        List<CompetenciaAjusteDto> competencias) {
}
