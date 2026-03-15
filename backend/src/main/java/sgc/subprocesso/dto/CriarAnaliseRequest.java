package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.MsgValidacao;
import sgc.comum.model.*;
import sgc.subprocesso.model.*;

@Builder
public record CriarAnaliseRequest(
        @TituloEleitoral
        String tituloUsuario,

        @Size(max = 500, message = MsgValidacao.OBSERVACOES_MAX_500)
        @Nullable
        String observacoes,

        @NotNull(message = MsgValidacao.SIGLA_UNIDADE_OBRIGATORIA)
        @Size(max = 20, message = MsgValidacao.SIGLA_UNIDADE_MAX)
        String siglaUnidade,

        @Size(max = 200, message = MsgValidacao.MOTIVO_MAX)
        @Nullable
        String motivo,

        @NotNull(message = MsgValidacao.ACAO_OBRIGATORIA)
        TipoAcaoAnalise acao
) {
}
