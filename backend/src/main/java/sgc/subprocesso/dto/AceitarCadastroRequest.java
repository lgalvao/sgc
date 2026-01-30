package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para requisição de aceite de cadastro (CDU-13 item 10 e CDU-14 item 11).
 */
@Builder
public record AceitarCadastroRequest(
        @NotBlank(message = "As observações são obrigatórias") @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes) {
}
