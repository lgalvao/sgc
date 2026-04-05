package sgc.subprocesso.dto;

import lombok.*;

import java.time.*;

/**
 * Command interno para atualização dos prazos e datas de etapa do subprocesso.
 */
@Builder
public record AtualizarPrazosSubprocessoCommand(
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa2
) {}
