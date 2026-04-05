package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;
import java.util.*;

/**
 * Command interno para atualização dos prazos e datas de etapa do subprocesso.
 */
@Builder
public record AtualizarPrazosSubprocessoCommand(
        Optional<LocalDateTime> dataLimiteEtapa1,
        Optional<LocalDateTime> dataFimEtapa1,
        Optional<LocalDateTime> dataLimiteEtapa2,
        Optional<LocalDateTime> dataFimEtapa2
) {}
