package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record SituacoesCapacitacaoRequest(
        @NotEmpty List<SituacaoCapacitacaoDto> situacoes
) {
}
