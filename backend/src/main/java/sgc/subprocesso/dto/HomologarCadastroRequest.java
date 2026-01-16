package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para requisição de homologação de cadastro (CDU-13 item 11 e CDU-14 item
 * 12).
 */
@Getter
@Builder
@AllArgsConstructor
public class HomologarCadastroRequest {
    /**
     * Observações adicionais.
     */
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private final String observacoes;
}
