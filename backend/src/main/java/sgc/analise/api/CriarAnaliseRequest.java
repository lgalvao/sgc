package sgc.analise.api;

import lombok.Builder;
import lombok.Getter;
import sgc.analise.internal.model.TipoAcaoAnalise;
import sgc.analise.internal.model.TipoAnalise;

@Getter
@Builder
public class CriarAnaliseRequest {
    private final Long codSubprocesso;
    private final TipoAnalise tipo;
    private final TipoAcaoAnalise acao;
    private final String siglaUnidade;
    private final String tituloUsuario;
    private final String motivo;
    private final String observacoes;
}
