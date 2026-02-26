package sgc.subprocesso.dto;

import lombok.*;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

@Builder
public record CriarAnaliseCommand(
        @TituloEleitoral
        String tituloUsuario,
        TipoAnalise tipo,
        TipoAcaoAnalise acao,
        Long codSubprocesso,
        String siglaUnidade,
        @org.jspecify.annotations.Nullable String motivo,
        @org.jspecify.annotations.Nullable String observacoes
) {
}
