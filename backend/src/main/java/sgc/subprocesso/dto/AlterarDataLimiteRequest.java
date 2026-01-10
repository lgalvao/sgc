package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AlterarDataLimiteRequest {
    @NotNull(message = "A nova data limite é obrigatória")
    private LocalDate novaDataLimite;
}
