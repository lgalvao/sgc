package sgc.diagnostico.dto;

import lombok.*;

@Builder
public record CompetenciaResumoDto(
        Long competenciaCodigo,
        String descricao
) {
}