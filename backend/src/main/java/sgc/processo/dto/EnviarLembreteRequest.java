package sgc.processo.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Builder
public record EnviarLembreteRequest(
        @NotNull(message = MsgValidacao.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
