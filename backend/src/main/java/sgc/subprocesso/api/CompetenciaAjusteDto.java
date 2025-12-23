package sgc.subprocesso.api;

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
    @NotNull
    private final Long codCompetencia;

    @NotBlank
    private final String nome;

    @NotNull
    @Valid
    private final List<AtividadeAjusteDto> atividades;
}
