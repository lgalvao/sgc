package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.MapaViews;
import sgc.mapa.model.TipoImpactoAtividade;

import java.util.List;

/**
 * DTO que representa uma atividade que sofreu alteração durante a revisão do cadastro.
 */
@Builder
public record AtividadeImpactadaDto(
        @JsonView(MapaViews.Publica.class)
        Long codigo,
        @JsonView(MapaViews.Publica.class)
        String descricao,
        @JsonView(MapaViews.Publica.class)
        TipoImpactoAtividade tipoImpacto,
        @JsonView(MapaViews.Publica.class)
        String descricaoAnterior,
        @JsonView(MapaViews.Publica.class)
        List<String> competenciasVinculadas) {
}
