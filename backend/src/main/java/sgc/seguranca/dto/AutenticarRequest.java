package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.comum.model.*;

/**
 * DTO para a requisição de autenticação de um usuário.
 */
@Builder
public record AutenticarRequest(
        @TituloEleitoral
        String tituloEleitoral,

        @NotNull(message = Mensagens.SENHA_OBRIGATORIA)
        @Size(max = 64, message = Mensagens.SENHA_MAX)
        String senha) {
}
