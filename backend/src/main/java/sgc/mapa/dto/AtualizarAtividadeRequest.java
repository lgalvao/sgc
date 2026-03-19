package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;
import sgc.seguranca.sanitizacao.*;

/**
 * Request para atualização de Atividade.
 */
@Builder
public record AtualizarAtividadeRequest(
        @NotBlank(message = SgcMensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
