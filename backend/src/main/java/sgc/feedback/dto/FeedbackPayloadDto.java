package sgc.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import sgc.comum.Mensagens;
import sgc.feedback.FeedbackTipo;
import sgc.seguranca.sanitizacao.SanitizarHtmlFormatado;
import tools.jackson.databind.JsonNode;

/**
 * Payload enviado pelo widget de feedback no campo {@code data} do formulário multipart.
 *
 * @param tipo      classificação do feedback
 * @param nota      descrição textual (10 a 500 caracteres)
 * @param metadados contexto capturado automaticamente pelo cliente (apenas informativo)
 */
public record FeedbackPayloadDto(
        @NotNull FeedbackTipo tipo,
        @NotBlank @Size(min = 10, max = 500, message = Mensagens.FEEDBACK_NOTA_MIN) @SanitizarHtmlFormatado String nota,
        @Nullable JsonNode metadados
) {
}
