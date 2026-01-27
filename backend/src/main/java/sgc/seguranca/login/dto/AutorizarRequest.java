package sgc.seguranca.login.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para a requisição de autorização de um usuário.
 */
@Builder
public record AutorizarRequest(
                @NotNull(message = "O título eleitoral é obrigatório.") @Size(max = 12, message = "O título eleitoral deve ter no máximo 12 caracteres.") @Pattern(regexp = "^\\d+$", message = "O título eleitoral deve conter apenas números.") String tituloEleitoral) {
}
