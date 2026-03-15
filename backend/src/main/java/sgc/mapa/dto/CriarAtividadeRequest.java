package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarAtividadeRequest(
        @NotNull(message = MsgValidacao.CODIGO_MAPA_OBRIGATORIO)
        Long mapaCodigo,

        @NotBlank(message = MsgValidacao.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
