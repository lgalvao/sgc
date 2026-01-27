package sgc.seguranca.login.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
                @NotNull(message = "O título eleitoral é obrigatório.") @Size(max = 20, message = "O título eleitoral deve ter no máximo 20 caracteres.") String tituloEleitoral,

                @NotNull(message = "O perfil é obrigatório.") @Size(max = 50, message = "O perfil deve ter no máximo 50 caracteres.") String perfil,

                @NotNull(message = "O código da unidade é obrigatório.") Long unidadeCodigo) {
}
