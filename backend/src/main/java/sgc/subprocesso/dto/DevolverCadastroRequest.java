package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de devolução de cadastro (CDU-13 item 9 e CDU-14 item
 * 10).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolverCadastroRequest {
    /**
     * Observações adicionais.
     */
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}
