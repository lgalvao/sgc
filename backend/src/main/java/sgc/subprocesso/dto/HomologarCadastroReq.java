package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de homologação de cadastro (CDU-13 item 11 e CDU-14 item
 * 12).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomologarCadastroReq {
    /**
     * Observações adicionais.
     */
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}
