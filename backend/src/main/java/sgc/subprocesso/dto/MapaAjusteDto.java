package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO para mapa de competências no contexto de ajustes.
 * CDU-16 item 4
 */
public record MapaAjusteDto(
    @NotNull Long mapaId,
    @NotBlank String unidadeNome,
    @NotNull @Valid List<CompetenciaAjusteDto> competencias,
    String justificativaDevolucao
) {}