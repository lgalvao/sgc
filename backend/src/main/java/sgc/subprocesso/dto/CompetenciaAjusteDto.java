package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para competência no contexto de ajustes do mapa.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class CompetenciaAjusteDto {

    @NotNull(message = "O código da competência é obrigatório")
    private final Long codCompetencia;

    @NotBlank(message = "O nome da competência é obrigatório")
    private final String nome;

    @NotNull(message = "A lista de atividades é obrigatória")
    @Valid
    private final List<AtividadeAjusteDto> atividades;
}
