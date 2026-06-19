package sgc.mapa.dto;

import lombok.Builder;

import java.util.Collections;
import java.util.List;

/**
 * Representa os impactos causados pela disponibilização de um mapa.
 */
@Builder
public record ImpactoMapaResponse(
        boolean temImpactos,
        List<AtividadeImpactadaDto> inseridas,
        List<AtividadeImpactadaDto> removidas,
        List<AtividadeImpactadaDto> alteradas,
        List<CompetenciaImpactadaDto> competenciasImpactadas,
        int totalInseridas,
        int totalRemovidas,
        int totalAlteradas,
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
