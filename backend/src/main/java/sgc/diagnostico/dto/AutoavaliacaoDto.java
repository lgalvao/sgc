package sgc.diagnostico.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record AutoavaliacaoDto(
        List<AvaliacaoCompetenciaDto> competencias,
        String situacaoServidor
) {
}
