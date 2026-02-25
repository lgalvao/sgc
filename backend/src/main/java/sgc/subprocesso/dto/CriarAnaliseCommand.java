package sgc.subprocesso.dto;

import lombok.Builder;
import sgc.comum.model.TituloEleitoral;
import sgc.subprocesso.model.TipoAcaoAnalise;
import sgc.subprocesso.model.TipoAnalise;

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
