package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * DTO de requisição para alterar data limite de subprocesso.
 */
@Builder
public record AlterarDataLimiteRequest(
                @NotNull(message = "A nova data limite é obrigatória") LocalDate novaDataLimite) {
}
