package sgc.mapa.dto.visualizacao;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.MapaViews;

import java.util.List;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        @JsonView(MapaViews.Publica.class)
        Long codigo,
        
        @JsonView(MapaViews.Publica.class)
        String descricao,
        
        @JsonView(MapaViews.Publica.class)
        List<Conhecimento> conhecimentos) {
}
