package sgc.relatorio;

import lombok.Builder;

import java.util.List;

@Builder
public record RelatorioDiagnosticoSituacaoCapacitacaoDto(
        Long codigoUnidade,
        String siglaUnidade,
        String nomeUnidade,
        List<RelatorioDiagnosticoSituacaoCapacitacaoCompetenciaDto> competencias
) {
}
