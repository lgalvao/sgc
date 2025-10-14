package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO para mapa de competências no contexto de ajustes.
 * CDU-16 item 4
 */
@Getter
@Builder
public class MapaAjusteDto {
    @NotNull private final Long mapaId;
    @NotBlank private final String unidadeNome;
    @NotNull @Valid private final List<CompetenciaAjusteDto> competencias;
    private final String justificativaDevolucao;
}