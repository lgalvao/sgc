package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.organizacao.dto.*;
import sgc.mapa.model.*;

import java.util.*;

/**
 * Resposta para visualização completa de um mapa de competências.
 */
@Builder
public record MapaVisualizacaoResponse(
        @JsonView(MapaViews.Publica.class)
        UnidadeResumoDto unidade,

        @JsonView(MapaViews.Publica.class)
        List<CompetenciaMapaDto> competencias,

        @JsonView(MapaViews.Publica.class)
        List<AtividadeMapaDto> atividadesSemCompetencia,

        @JsonView(MapaViews.Publica.class)
        String sugestoes
) {
}
