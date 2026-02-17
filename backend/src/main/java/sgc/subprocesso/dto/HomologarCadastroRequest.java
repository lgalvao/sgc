package sgc.subprocesso.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * DTO para requisição de homologação de cadastro
 */
@Builder
public record HomologarCadastroRequest(
        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        String observacoes) {
}
