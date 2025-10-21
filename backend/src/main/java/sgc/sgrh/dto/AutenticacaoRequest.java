package sgc.sgrh.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@AllArgsConstructor
@Jacksonized
/**
 * DTO para a requisição de autenticação de um usuário.
 *
 * @param tituloEleitoral O título de eleitor do usuário.
 * @param senha A senha do usuário.
 */
public class AutenticacaoRequest {
    @NotNull(message = "O título eleitoral é obrigatório.")
    Long tituloEleitoral;
    @NotNull(message = "A senha é obrigatória.")
    String senha;
}