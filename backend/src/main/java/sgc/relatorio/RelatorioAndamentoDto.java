package sgc.relatorio;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

@Builder
public record RelatorioAndamentoDto(
        String siglaUnidade,
        String nomeUnidade,
        String situacaoAtual,
        String localizacao,
        LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataFimEtapa2,
        @Nullable LocalDateTime dataUltimaMovimentacao,
        String responsavel,
        String titular
) {
}
