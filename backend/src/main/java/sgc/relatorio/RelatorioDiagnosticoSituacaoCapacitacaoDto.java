package sgc.relatorio;

import lombok.*;

import java.util.*;

@Builder
public record RelatorioDiagnosticoSituacaoCapacitacaoDto(
        Long codigoUnidade,
        String siglaUnidade,
        String nomeUnidade,
        List<RelatorioDiagnosticoSituacaoCapacitacaoCompetenciaDto> competencias
) {
}
