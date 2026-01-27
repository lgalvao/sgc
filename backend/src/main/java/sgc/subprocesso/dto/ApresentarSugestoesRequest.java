package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import sgc.seguranca.sanitizacao.SanitizarHtml;

/**
 * Request para apresentar sugestões ao mapa de competências (CDU-19 item 8).
 * Usado pelo CHEFE da
 * unidade para fornecer feedback sobre o mapa disponibilizado.
 */
@Builder
public record ApresentarSugestoesRequest(
                /**
                 * Texto com as sugestões do CHEFE (obrigatório).
                 */
                @NotBlank(message = "As sugestões são obrigatórias") @SanitizarHtml String sugestoes) {
}
