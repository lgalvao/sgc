package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.MapaViews;

import java.util.Collections;
import java.util.List;

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
    List<CompetenciaImpactadaDto> competenciasImpactadas
) {
    public static ImpactoMapaResponse semImpacto() {
        return ImpactoMapaResponse.builder()
                .temImpactos(false)
                .inseridas(Collections.emptyList())
                .removidas(Collections.emptyList())
                .alteradas(Collections.emptyList())
                .competenciasImpactadas(Collections.emptyList())
                .build();
    }
}
