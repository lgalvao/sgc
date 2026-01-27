package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO para resposta de mensagem genérica.
 */
@Builder
public record MensagemResponse(
        /**
         * A mensagem de retorno.
         */
        String mensagem) {
}
