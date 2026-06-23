package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public record ValidarDiagnosticosEmBlocoRequest(
        @NotEmpty List<Long> subprocessos
) {
}