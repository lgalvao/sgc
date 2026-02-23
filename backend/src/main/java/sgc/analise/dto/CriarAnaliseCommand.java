package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.TituloEleitoral;

@Builder
public record CriarAnaliseCommand(
        @TituloEleitoral
        String tituloUsuario,
        TipoAnalise tipo,
        TipoAcaoAnalise acao,
        Long codSubprocesso,
        String siglaUnidade,
        String motivo,
        String observacoes
) {
}
