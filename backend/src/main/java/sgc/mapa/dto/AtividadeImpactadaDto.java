package sgc.mapa.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import sgc.mapa.model.*;

import java.util.*;

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
        List<String> conhecimentos,
        @JsonView(MapaViews.Publica.class)
        List<String> competenciasVinculadas) {
}
