package sgc.subprocesso.dto;

import com.fasterxml.jackson.annotation.JsonView;
import sgc.mapa.model.Mapa;
import sgc.mapa.model.MapaViews;
import sgc.mapa.dto.visualizacao.AtividadeDto;
import sgc.organizacao.model.Unidade;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoViews;

import java.util.List;

/**
 * Resposta agregada para o contexto de edição de um subprocesso.
 */
public record ContextoEdicaoResponse(
    @JsonView(SubprocessoViews.Publica.class)
    Unidade unidade,
    
    @JsonView(SubprocessoViews.Publica.class)
    Subprocesso subprocesso,
    
    @JsonView(SubprocessoViews.Publica.class)
    SubprocessoDetalheResponse detalhes,
    
    @JsonView(MapaViews.Publica.class)
    Mapa mapa,
    
    @JsonView(SubprocessoViews.Publica.class)
    List<AtividadeDto> atividadesDisponiveis
) {}
