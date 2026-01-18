package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para requisição de aceite de cadastro (CDU-13 item 10 e CDU-14 item 11).
 */
@Getter
@Builder
@AllArgsConstructor
public class AceitarCadastroRequest {
    /**
     * Observações adicionais.
     */
    @NotBlank(message = "As observações são obrigatórias")
    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private final String observacoes;
}
