package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * Request para salvar ajustes no mapa de competências.
 * CDU-16 item 5
 */
public record SalvarAjustesReq(
    @NotNull @Valid List<CompetenciaAjusteDto> competencias
) {
    public SalvarAjustesReq {
        competencias = new ArrayList<>(competencias);
    }

    @Override
    public List<CompetenciaAjusteDto> competencias() {
        return new ArrayList<>(competencias);
    }
}