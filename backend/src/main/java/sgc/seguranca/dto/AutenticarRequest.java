package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.model.*;

/**
 * DTO para a requisição de autenticação de um usuário.
 */
@Builder
public record AutenticarRequest(
        @TituloEleitoral
        String tituloEleitoral,

        @NotNull(message = "A senha é obrigatória.")
        @Size(max = 64, message = "A senha deve ter no máximo 64 caracteres.")
        String senha) {
}
