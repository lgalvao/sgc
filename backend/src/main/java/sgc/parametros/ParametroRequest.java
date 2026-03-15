package sgc.parametros;

import jakarta.validation.constraints.*;
import sgc.comum.MsgValidacao;

/**
 * DTO de requisição para atualização de parâmetro de configuração.
 * Utilizado na atualização em bloco de parâmetros.
 */
public record ParametroRequest(
        @NotNull(message = MsgValidacao.CODIGO_PARAMETRO_OBRIGATORIO)
        Long codigo,

        @NotBlank(message = MsgValidacao.CHAVE_OBRIGATORIA)
        @Size(max = 50, message = MsgValidacao.CHAVE_MAX)
        String chave,

        String descricao,

        @NotBlank(message = MsgValidacao.VALOR_OBRIGATORIO)
        String valor
) {
}
