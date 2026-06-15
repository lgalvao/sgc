package sgc.relatorio;

import lombok.Builder;

@Builder
public record RelatorioDiagnosticoSituacaoCapacitacaoCompetenciaDto(
        Long competenciaCodigo,
        String competenciaDescricao,
        int totalNaoSeAplica,
        int totalACapacitar,
        int totalEmCapacitacao,
        int totalCapacitado,
        int totalInstrutor
) {
}
