package sgc.mapa.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

/**
 * Request para atualização de Conhecimento.
 */
@Builder
public record AtualizarConhecimentoRequest(
        @NotBlank(message = SgcMensagens.DESCRICAO_NAO_PODE_SER_VAZIA)
        @SanitizarHtml
        String descricao
) {
}
