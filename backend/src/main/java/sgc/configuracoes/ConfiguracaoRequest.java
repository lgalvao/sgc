package sgc.configuracoes;

import jakarta.validation.constraints.*;
import sgc.comum.*;

/**
 * DTO de requisição para atualização de Configuração de configuração.
 * Utilizado na atualização em bloco de Configuraçãos.
 */
public record ConfiguracaoRequest(
        @NotNull(message = Mensagens.CODIGO_PARAMETRO_OBRIGATORIO)
        Long codigo,

        @NotBlank(message = Mensagens.CHAVE_OBRIGATORIA)
        @Size(max = 50, message = Mensagens.CHAVE_MAX)
        String chave,

        String descricao,

        @NotBlank(message = Mensagens.VALOR_OBRIGATORIO)
        String valor
) {
}
