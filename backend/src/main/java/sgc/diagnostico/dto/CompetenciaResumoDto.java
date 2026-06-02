package sgc.diagnostico.dto;

import lombok.Builder;

@Builder
public record CompetenciaResumoDto(
        Long competenciaCodigo,
        String descricao
) {
}