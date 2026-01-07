package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AlterarDataLimiteRequest {
    @NotNull
    private LocalDate novaDataLimite;
}
