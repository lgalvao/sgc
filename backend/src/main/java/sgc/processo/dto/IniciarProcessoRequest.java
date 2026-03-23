package sgc.processo.dto;

import jakarta.validation.constraints.*;
import sgc.comum.*;
import sgc.processo.model.*;

import java.util.*;

public record IniciarProcessoRequest(
        @NotNull(message = Mensagens.TIPO_PROCESSO_OBRIGATORIO)
        TipoProcesso tipo,

        List<Long> unidades
) {
}
