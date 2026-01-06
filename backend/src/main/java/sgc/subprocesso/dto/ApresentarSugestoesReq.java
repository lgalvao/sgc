package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.seguranca.SanitizarHtml;

/**
 * Request para apresentar sugestões ao mapa de competências (CDU-19 item 8). Usado pelo CHEFE da
 * unidade para fornecer feedback sobre o mapa disponibilizado.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApresentarSugestoesReq {
    /**
     * Texto com as sugestões do CHEFE (obrigatório).
     */
    @NotBlank(message = "As sugestões são obrigatórias")
    @SanitizarHtml
    private String sugestoes;
}
