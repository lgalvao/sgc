package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.subprocesso.model.*;

@Builder
public record CriarAnaliseRequest(
        @Size(max = 500, message = Mensagens.OBSERVACOES_MAX_500)
        @Nullable
        String observacoes,

        @Size(max = 200, message = Mensagens.MOTIVO_MAX)
        @Nullable
        String motivo,

        @NotNull(message = Mensagens.ACAO_OBRIGATORIA)
        TipoAcaoAnalise acao
) {
}
