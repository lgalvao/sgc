package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

@Builder
public record CriarAnaliseCommand(
        String tituloUsuario,
        Long codSubprocesso,
        TipoAnalise tipo,
        TipoAcaoAnalise acao,
        String siglaUnidade,
        String motivo,
        String observacoes
) {
}
