package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.MapaViews;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças nas
 * atividades durante a
 * revisão do cadastro.
 *
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
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
