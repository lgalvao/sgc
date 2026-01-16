package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para devolver validação do mapa (CDU-20 item 7).
 */
@Getter
@Builder
@AllArgsConstructor
public class DevolverValidacaoRequest {
    /**
     * A justificativa para a devolução.
     */
    @NotBlank(message = "A justificativa é obrigatória")
    @SanitizarHtml
    private final String justificativa;
}
