package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.mapa.model.*;
import sgc.organizacao.model.*;

import java.util.*;

/**
 * Resposta para visualização completa de um mapa de competências.
 */
@Builder
public record MapaVisualizacaoResponse(
        @JsonView(MapaViews.Publica.class)
        Unidade unidade,

        @JsonView(MapaViews.Publica.class)
        List<Competencia> competencias,

        @JsonView(MapaViews.Publica.class)
        List<Atividade> atividadesSemCompetencia,

        @JsonView(MapaViews.Publica.class)
        String sugestoes
) {
}
