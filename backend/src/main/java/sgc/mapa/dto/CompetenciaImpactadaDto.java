package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.mapa.model.*;

import java.util.*;

/**
 * DTO que representa uma competência que foi impactada por mudanças nas atividades na revisão.
 */
@Builder
public record CompetenciaImpactadaDto(
        @JsonView(MapaViews.Publica.class)
        Long codigo,

        @JsonView(MapaViews.Publica.class)
        String descricao,

        @JsonView(MapaViews.Publica.class)
        List<String> atividadesAfetadas,

        @JsonView(MapaViews.Publica.class)
        List<TipoImpactoCompetencia> tiposImpacto) {
}
