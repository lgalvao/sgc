package sgc.seguranca.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.comum.Mensagens;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
        @NotNull(message = Mensagens.PERFIL_OBRIGATORIO) @Size(max = 50, message = Mensagens.PERFIL_MAX) String perfil,

        @NotNull(message = Mensagens.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
