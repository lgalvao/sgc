package sgc.processo.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 */
@Builder
public record EnviarLembreteRequest(
        @NotNull(message = SgcMensagens.CODIGO_UNIDADE_OBRIGATORIO) Long unidadeCodigo) {
}
