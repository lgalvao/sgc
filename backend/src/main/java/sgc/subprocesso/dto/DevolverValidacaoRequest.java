package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para devolver validação do mapa (CDU-20 item 7).
 */
@Builder
public record DevolverValidacaoRequest(
                /**
                 * A justificativa para a devolução.
                 */
                @NotBlank(message = "A justificativa é obrigatória") @SanitizarHtml String justificativa) {
}
