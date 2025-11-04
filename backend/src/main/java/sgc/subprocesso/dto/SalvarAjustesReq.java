package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request para salvar ajustes no mapa de competências.
 * CDU-16 item 5
 *
 * @param competencias A lista de competências com os ajustes a serem salvos.
 */
public record SalvarAjustesReq(
    @NotNull @Valid List<CompetenciaAjusteDto> competencias
) {
}