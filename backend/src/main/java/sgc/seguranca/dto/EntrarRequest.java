package sgc.seguranca.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;
import sgc.comum.model.*;

/**
 * DTO para a requisição de entrada (login) do usuário.
 */
@Builder
public record EntrarRequest(
        @TituloEleitoral String tituloEleitoral,

        @NotNull(message = MsgValidacao.PERFIL_OBRIGATORIO) @Size(max = 50, message = MsgValidacao.PERFIL_MAX) String perfil,

        @NotNull(message = MsgValidacao.CODIGO_UNIDADE_OBRIGATORIO_COM_PONTO) Long unidadeCodigo) {
}
