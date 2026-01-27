package sgc.seguranca.login.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para a requisição de autenticação de um usuário.
 */
@Builder
public record AutenticarRequest(
                @NotNull(message = "O título é obrigatório.") @Size(max = 12, message = "O título deve ter no máximo 12 caracteres.") @Pattern(regexp = "^\\d+$", message = "O título eleitoral deve conter apenas números.") String tituloEleitoral,

                @NotNull(message = "A senha é obrigatória.") @Size(max = 64, message = "A senha deve ter no máximo 64 caracteres.") String senha) {
}
