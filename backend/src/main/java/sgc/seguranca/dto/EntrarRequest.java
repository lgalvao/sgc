package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
        @NotNull(message = SgcMensagens.PERFIL_OBRIGATORIO) @Size(max = 50, message = SgcMensagens.PERFIL_MAX) String perfil,

        @NotNull(message = SgcMensagens.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
