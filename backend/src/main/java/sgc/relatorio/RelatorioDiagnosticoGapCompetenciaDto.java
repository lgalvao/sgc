package sgc.relatorio;

import lombok.*;

@Builder
public record RelatorioDiagnosticoGapCompetenciaDto(
        Long competenciaCodigo,
        String competenciaDescricao,
        Double mediaGap,
        int totalAvaliacoesConsideradas
) {
}
