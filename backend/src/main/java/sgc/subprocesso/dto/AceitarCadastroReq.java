package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de aceite de cadastro (CDU-13 item 10 e CDU-14 item 11).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AceitarCadastroReq {
    /**
     * Observações adicionais.
     */
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}
