package sgc.diagnostico.dto;

import lombok.*;

import java.util.*;

@Builder
public record DiagnosticoContextoDto(
        Long processoCodigo,
        Long subprocessoCodigo,
        Long unidadeCodigo,
        String unidadeSigla,
        String unidadeNome,
        String situacaoSubprocesso,
        String situacaoDiagnostico,
        List<CompetenciaResumoDto> competencias
) {
}
