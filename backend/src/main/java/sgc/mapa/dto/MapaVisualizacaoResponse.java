package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.Atividade;
import sgc.mapa.model.Competencia;
import sgc.mapa.model.MapaViews;
import sgc.organizacao.model.Unidade;

import java.util.List;

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
) {}
