package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para devolver validação do mapa
 */
@Builder
public record DevolverValidacaoRequest(
        @NotBlank(message = "A justificativa é obrigatória")
        @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")
        @SanitizarHtml
        String justificativa) {
}
