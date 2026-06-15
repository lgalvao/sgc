package sgc.relatorio;

import lombok.Builder;

import java.util.List;

@Builder
public record RelatorioDiagnosticoGapDto(
        Long codigoUnidade,
        String siglaUnidade,
        String nomeUnidade,
        List<RelatorioDiagnosticoGapCompetenciaDto> competencias
) {
}
