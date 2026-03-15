package sgc.processo.dto;

import jakarta.validation.constraints.*;
import sgc.comum.MsgValidacao;
import sgc.processo.model.*;

import java.util.*;

public record IniciarProcessoRequest(
        @NotNull(message = MsgValidacao.TIPO_PROCESSO_OBRIGATORIO_INICIAR)
        TipoProcesso tipo,

        List<Long> unidades
) {
}
