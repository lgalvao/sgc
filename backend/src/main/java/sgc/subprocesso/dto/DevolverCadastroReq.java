package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de devolução de cadastro (CDU-13 item 9 e CDU-14 item 10).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevolverCadastroReq {
    /** Observações adicionais. */
    private String observacoes;
}
