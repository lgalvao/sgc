package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
        @NotNull(message = Mensagens.PERFIL_OBRIGATORIO) @Size(max = 50, message = Mensagens.PERFIL_MAX) String perfil,

        @NotNull(message = Mensagens.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
