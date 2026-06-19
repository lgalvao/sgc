package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import sgc.comum.Mensagens;
import sgc.processo.model.TipoProcesso;

import java.util.List;

public record IniciarProcessoRequest(
        @NotNull(message = Mensagens.TIPO_PROCESSO_OBRIGATORIO)
        TipoProcesso tipo,

        List<Long> unidades
) {
}
