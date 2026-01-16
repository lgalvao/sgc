package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO de requisição para alterar data limite de subprocesso.
 */
@Getter
@Builder
@AllArgsConstructor
public class AlterarDataLimiteRequest {
    @NotNull(message = "A nova data limite é obrigatória")
    private final LocalDate novaDataLimite;
}
