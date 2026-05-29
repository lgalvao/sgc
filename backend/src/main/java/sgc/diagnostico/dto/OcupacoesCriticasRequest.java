package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OcupacoesCriticasRequest(
        @NotEmpty List<OcupacaoCriticaDto> ocupacoes
) {
}