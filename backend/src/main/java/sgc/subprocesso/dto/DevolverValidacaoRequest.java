package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para devolver validação do mapa
 */
@Builder
public record DevolverValidacaoRequest(
        @NotBlank(message = "A justificativa é obrigatória") @SanitizarHtml String justificativa) {
}
