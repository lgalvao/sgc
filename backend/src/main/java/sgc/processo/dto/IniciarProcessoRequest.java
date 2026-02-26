package sgc.processo.dto;

import jakarta.validation.constraints.*;
import sgc.processo.model.*;

import java.util.*;

public record IniciarProcessoRequest(
        @NotNull(message = "O tipo do processo é obrigatório")
        TipoProcesso tipo,

        List<Long> unidades
) {
}
