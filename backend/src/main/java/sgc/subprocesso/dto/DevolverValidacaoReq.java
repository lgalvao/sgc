package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.comum.json.SanitizeHtml;

/**
 * Request para devolver validação do mapa (CDU-20 item 7).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolverValidacaoReq {
    /** A justificativa para a devolução. */
    @NotBlank
    @SanitizeHtml
    private String justificativa;
}
