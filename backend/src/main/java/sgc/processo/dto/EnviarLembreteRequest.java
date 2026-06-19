package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import sgc.comum.Mensagens;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Builder
public record EnviarLembreteRequest(
        @NotNull(message = Mensagens.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
