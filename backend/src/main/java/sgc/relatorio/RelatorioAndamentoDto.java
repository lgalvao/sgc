package sgc.relatorio;

import lombok.*;

import java.time.*;

@Builder
public record RelatorioAndamentoDto(
        String siglaUnidade,
        String nomeUnidade,
        String situacaoAtual,
        LocalDateTime dataUltimaMovimentacao,
        String responsavel,
        String titular
) {
}
