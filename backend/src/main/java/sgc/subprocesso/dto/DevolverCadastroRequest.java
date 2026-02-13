package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de devolução de cadastro (CDU-13 item 9 e CDU-14 item
 * 10).
 */
public record DevolverCadastroRequest(
        @NotBlank(message = "As observações são obrigatórias") @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres") String observacoes) {
}
