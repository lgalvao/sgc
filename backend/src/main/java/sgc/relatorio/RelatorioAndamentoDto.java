package sgc.relatorio;

import lombok.Builder;

import java.time.LocalDateTime;

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
