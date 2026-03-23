package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

/**
 * Request para atualização de Atividade.
 */
@Builder
public record AtualizarAtividadeRequest(
        @NotBlank(message = Mensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
