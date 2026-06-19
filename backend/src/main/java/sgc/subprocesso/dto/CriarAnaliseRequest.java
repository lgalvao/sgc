package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.jspecify.annotations.Nullable;
import sgc.comum.Mensagens;
import sgc.subprocesso.model.TipoAcaoAnalise;

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
