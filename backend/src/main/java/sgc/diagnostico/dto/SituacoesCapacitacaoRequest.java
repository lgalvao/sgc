package sgc.diagnostico.dto;

import jakarta.validation.constraints.*;

import java.util.*;

public record SituacoesCapacitacaoRequest(
        @NotEmpty List<SituacaoCapacitacaoDto> situacoes
) {
}
