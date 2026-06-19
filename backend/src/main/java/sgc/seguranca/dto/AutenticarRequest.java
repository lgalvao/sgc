package sgc.seguranca.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.comum.model.TituloEleitoral;

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
