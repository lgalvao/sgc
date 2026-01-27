package sgc.analise.dto;

import lombok.Builder;
import sgc.analise.model.TipoAcaoAnalise;
import sgc.analise.model.TipoAnalise;

@Builder
public record CriarAnaliseCommand(
        Long codSubprocesso,
        TipoAnalise tipo,
        TipoAcaoAnalise acao,
        String siglaUnidade,
        String tituloUsuario,
        String motivo,
        String observacoes
) {
}
