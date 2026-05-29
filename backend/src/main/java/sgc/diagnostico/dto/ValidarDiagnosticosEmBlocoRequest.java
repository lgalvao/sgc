package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ValidarDiagnosticosEmBlocoRequest(
        @NotEmpty List<Long> subprocessos
) {
}