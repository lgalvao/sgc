package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import sgc.processo.model.TipoProcesso;

import java.util.List;

public record IniciarProcessoRequest(
        @NotNull(message = "O tipo do processo é obrigatório")
        TipoProcesso tipo,

        List<Long> unidades
) {
}
