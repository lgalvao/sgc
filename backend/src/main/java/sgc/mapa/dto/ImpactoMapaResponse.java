package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.mapa.model.*;

import java.util.*;

/**
 * Representa os impactos causados pela disponibilização de um mapa.
 */
@Builder
public record ImpactoMapaResponse(
        @JsonView(MapaViews.Publica.class)
        boolean temImpactos,

        @JsonView(MapaViews.Publica.class)
        List<AtividadeImpactadaDto> inseridas,

        @JsonView(MapaViews.Publica.class)
        List<AtividadeImpactadaDto> removidas,

        @JsonView(MapaViews.Publica.class)
        List<AtividadeImpactadaDto> alteradas,

        @JsonView(MapaViews.Publica.class)
        List<CompetenciaImpactadaDto> competenciasImpactadas,

        @JsonView(MapaViews.Publica.class)
        int totalInseridas,

        @JsonView(MapaViews.Publica.class)
        int totalRemovidas,

        @JsonView(MapaViews.Publica.class)
        int totalAlteradas,

        @JsonView(MapaViews.Publica.class)
        int totalCompetenciasImpactadas
) {
    public static ImpactoMapaResponse semImpacto() {
        return ImpactoMapaResponse.builder()
                .temImpactos(false)
                .inseridas(Collections.emptyList())
                .removidas(Collections.emptyList())
                .alteradas(Collections.emptyList())
                .competenciasImpactadas(Collections.emptyList())
                .totalInseridas(0)
                .totalRemovidas(0)
                .totalAlteradas(0)
                .totalCompetenciasImpactadas(0)
                .build();
    }
}
