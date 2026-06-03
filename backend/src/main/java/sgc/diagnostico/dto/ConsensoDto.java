package sgc.diagnostico.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record ConsensoDto(
        List<AvaliacaoCompetenciaDto> competencias,
        List<ConsensoCompetenciaDto> competenciasDetalhadas,
        String situacaoServidor
) {
}
