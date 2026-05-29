package sgc.diagnostico.dto;

import java.util.List;

public record DiagnosticoContextoDto(
        Long processoCodigo,
        Long subprocessoCodigo,
        Long unidadeCodigo,
        String unidadeSigla,
        String unidadeNome,
        String situacaoSubprocesso,
        List<CompetenciaResumoDto> competencias
) {
}
