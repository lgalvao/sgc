package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

import java.util.List;

/**
 * Request para salvar os ajustes feitos no mapa de competências (CDU-16 item
 * 5).
 */
@Builder
public record SalvarAjustesRequest(
                /**
                 * Lista de competências com suas atividades e conhecimentos ajustados.
                 */
                @NotEmpty(message = "A lista de competências não pode ser vazia") List<CompetenciaAjusteDto> competencias) {
}
