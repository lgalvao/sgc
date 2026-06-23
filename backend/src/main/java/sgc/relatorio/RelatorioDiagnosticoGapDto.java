package sgc.relatorio;

import lombok.*;

import java.util.*;

@Builder
public record RelatorioDiagnosticoGapDto(
        Long codigoUnidade,
        String siglaUnidade,
        String nomeUnidade,
        List<RelatorioDiagnosticoGapCompetenciaDto> competencias
) {
}
