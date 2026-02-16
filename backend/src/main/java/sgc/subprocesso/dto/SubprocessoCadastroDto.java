package sgc.subprocesso.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import sgc.mapa.model.Conhecimento;
import sgc.mapa.model.MapaViews;

import java.util.List;

/**
 * DTO agregado retornado pelo endpoint GET /api/subprocessos/{codigo}/cadastro.
 */
@Getter
@Builder
public class SubprocessoCadastroDto {

    @JsonView(MapaViews.Publica.class)
    private final Long subprocessoCodigo;
    
    @JsonView(MapaViews.Publica.class)
    private final @Nullable String unidadeSigla;
    
    @JsonView(MapaViews.Publica.class)
    private final List<AtividadeCadastroDto> atividades;

    @Getter
    @Builder
    public static class AtividadeCadastroDto {
        @JsonView(MapaViews.Publica.class)
        private final Long codigo;
        
        @JsonView(MapaViews.Publica.class)
        private final String descricao;
        
        @JsonView(MapaViews.Publica.class)
        private final List<Conhecimento> conhecimentos;
    }
}
