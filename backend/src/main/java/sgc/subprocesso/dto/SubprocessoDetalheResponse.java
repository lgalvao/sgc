package sgc.subprocesso.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import sgc.organizacao.model.Usuario;
import sgc.subprocesso.model.Movimentacao;
import sgc.subprocesso.model.Subprocesso;
import sgc.subprocesso.model.SubprocessoViews;

import java.util.List;

@Builder
public record SubprocessoDetalheResponse(
    @JsonView(SubprocessoViews.Publica.class)
    Subprocesso subprocesso,
    
    @JsonView(SubprocessoViews.Publica.class)
    Usuario responsavel,
    
    @JsonView(SubprocessoViews.Publica.class)
    Usuario titular,
    
    @JsonView(SubprocessoViews.Publica.class)
    List<Movimentacao> movimentacoes,

    @JsonView(SubprocessoViews.Publica.class)
    String localizacaoAtual
) {}
