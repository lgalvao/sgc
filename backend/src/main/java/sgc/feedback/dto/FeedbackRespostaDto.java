package sgc.feedback.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Resposta retornada após o registro bem-sucedido de um feedback.
 *
 * @param codigo    identificador único do registro criado
 * @param enviadoEm data/hora de envio com fuso horário
 */
public record FeedbackRespostaDto(
        UUID codigo,
        OffsetDateTime enviadoEm
) {
}
