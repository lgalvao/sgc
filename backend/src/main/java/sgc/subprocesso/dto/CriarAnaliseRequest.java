package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

@Builder
public record CriarAnaliseRequest(
        @TituloEleitoral
        String tituloUsuario,

        @Size(max = 500, message = SgcMensagens.OBSERVACOES_MAX_500)
        @Nullable
        String observacoes,

        @NotBlank(message = SgcMensagens.SIGLA_OBRIGATORIA)
        @Size(max = 20, message = SgcMensagens.SIGLA_MAX)
        String siglaUnidade,

        @Size(max = 200, message = SgcMensagens.MOTIVO_MAX)
        @Nullable
        String motivo,

        @NotNull(message = SgcMensagens.ACAO_OBRIGATORIA)
        TipoAcaoAnalise acao
) {
}
