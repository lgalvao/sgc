package sgc.subprocesso.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AlterarDataLimiteRequest {
    @NotNull
    private LocalDate novaDataLimite;
}
