package sgc.subprocesso.dto;

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
    /** Observações adicionais. */
    private String observacoes;
}
