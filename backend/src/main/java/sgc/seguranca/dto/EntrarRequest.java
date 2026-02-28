package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.model.*;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
        @TituloEleitoral String tituloEleitoral,

        @NotNull(message = "O perfil é obrigatório.") @Size(max = 50, message = "O perfil deve ter no máximo 50 caracteres.") String perfil,

        @NotNull(message = "O código da unidade é obrigatório.") Long unidadeCodigo) {
}
