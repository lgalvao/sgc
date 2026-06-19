package sgc.relatorio;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RelatorioAndamentoDto(
        String siglaUnidade,
        String nomeUnidade,
        String situacaoAtual,
        String localizacao,
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataFimEtapa2,
        LocalDateTime dataUltimaMovimentacao,
        String responsavel,
        String titular
) {
}
