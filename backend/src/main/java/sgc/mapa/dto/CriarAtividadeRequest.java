package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

@Builder
public record CriarAtividadeRequest(
        @NotNull(message = Mensagens.CODIGO_MAPA_OBRIGATORIO)
        Long mapaCodigo,

        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
