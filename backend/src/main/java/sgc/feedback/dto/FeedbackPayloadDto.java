package sgc.feedback.dto;

import jakarta.validation.constraints.*;
import org.jspecify.annotations.*;
import sgc.comum.*;
import sgc.feedback.*;
import sgc.seguranca.sanitizacao.*;
import tools.jackson.databind.*;

/**
 * Payload enviado pelo widget de feedback no campo {@code data} do formulário multipart.
 *
 * @param tipo      classificação do feedback
 * @param nota      descrição textual (10 a 500 caracteres)
 * @param metadados contexto capturado automaticamente pelo cliente (apenas informativo)
 */
public record FeedbackPayloadDto(
        @NotNull FeedbackTipo tipo,
        @NotBlank @Size(min = 10, max = 500, message = Mensagens.OBSERVACOES_MAX_500) @SanitizarHtmlFormatado String nota,
        @Nullable JsonNode metadados
) {
}
