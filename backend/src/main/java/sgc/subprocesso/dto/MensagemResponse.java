package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de resposta genérica contendo uma mensagem de confirmação.
 * 
 * <p>Usado para retornar mensagens simples de sucesso em operações que não
 * precisam retornar dados específicos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MensagemResponse {

    private String message;
}
