package sgc.diagnostico.dto;

import lombok.Builder;
import java.util.List;

@Builder
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
