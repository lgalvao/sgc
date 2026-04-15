package sgc.relatorio;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record RelatorioAndamentoDto(
        String siglaUnidade,
        String nomeUnidade,
        String situacaoAtual,
        LocalDateTime dataLimite,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataFimEtapa2,
        LocalDateTime dataUltimaMovimentacao,
        String responsavel,
        String titular
) {
}
