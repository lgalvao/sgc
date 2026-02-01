package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para apresentar sugestões ao mapa de competências (CDU-19 item 8).
 * Usado pelo CHEFE da
 * unidade para fornecer feedback sobre o mapa disponibilizado.
 */
@Builder
public record ApresentarSugestoesRequest(
        @NotBlank(message = "As sugestões são obrigatórias") @Size(max = 1000, message = "As sugestões devem ter no máximo 1000 caracteres") @SanitizarHtml String sugestoes) {
}
