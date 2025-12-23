package sgc.subprocesso.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de homologação de cadastro (CDU-13 item 11 e CDU-14 item 12).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomologarCadastroReq {
    /**
     * Observações adicionais.
     */
    private String observacoes;
}
