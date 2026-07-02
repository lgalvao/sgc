package sgc.relatorio;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record RelatorioDiagnosticoGapCompetenciaDto(
        Long competenciaCodigo,
        String competenciaDescricao,
        @Nullable Double mediaGap,
        int totalAvaliacoesConsideradas
) {
}
