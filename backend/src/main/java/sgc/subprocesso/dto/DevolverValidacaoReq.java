package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import sgc.comum.json.SanitizeHtml;

/**
 * Request para devolver validação do mapa (CDU-20 item 7).
 *
 * @param justificativa A justificativa para a devolução.
 */
public record DevolverValidacaoReq(
    @NotBlank
    @SanitizeHtml
    String justificativa
) {}