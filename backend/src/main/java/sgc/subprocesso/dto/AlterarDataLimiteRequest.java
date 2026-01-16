package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de requisição para alterar data limite de subprocesso.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlterarDataLimiteRequest {
    @NotNull(message = "A nova data limite é obrigatória")
    private LocalDate novaDataLimite;
}
