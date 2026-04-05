package sgc.subprocesso.dto;

import org.jspecify.annotations.*;

import java.time.*;

/**
 * Command interno para atualização de subprocesso.
 */
public record AtualizarSubprocessoCommand(
        Long codUnidade,
        Long codMapa,
        @Nullable LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2
) {
}
