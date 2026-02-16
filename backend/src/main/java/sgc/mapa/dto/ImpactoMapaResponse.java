package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
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
    List<Atividade> inseridas,
    
    @JsonView(MapaViews.Publica.class)
    List<Atividade> removidas,
    
    @JsonView(MapaViews.Publica.class)
    List<Atividade> alteradas,
    
    @JsonView(MapaViews.Publica.class)
    List<Competencia> competenciasImpactadas
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
