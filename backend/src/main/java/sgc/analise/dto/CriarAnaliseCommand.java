package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;
import sgc.comum.validacao.TituloEleitoral;

@Builder
public record CriarAnaliseCommand(
        @TituloEleitoral
        String tituloUsuario,

        Long codSubprocesso,
        TipoAnalise tipo,
        TipoAcaoAnalise acao,
        String siglaUnidade,
        String motivo,
        String observacoes
) {
}
