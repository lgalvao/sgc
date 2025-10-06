package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * Request para salvar ajustes no mapa de competências.
 * CDU-16 item 5
 */
public record SalvarAjustesRequest(
    @NotNull @Valid List<CompetenciaAjusteDTO> competencias
) {}