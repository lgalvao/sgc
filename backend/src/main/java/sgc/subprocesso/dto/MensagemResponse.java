package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO de resposta genérica contendo uma mensagem de confirmação.
 * 
 * <p>Usado para retornar mensagens simples de sucesso em operações que não
 * precisam retornar dados específicos.
 */
@Getter
@Builder
@AllArgsConstructor
public class MensagemResponse {

    private final String message;
}
