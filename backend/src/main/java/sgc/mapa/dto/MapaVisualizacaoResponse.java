package sgc.mapa.dto;

import lombok.Builder;
import sgc.organizacao.dto.UnidadeResumoDto;

import java.util.List;

/**
 * Resposta para visualização completa de um mapa de competências.
 */
@Builder
public record MapaVisualizacaoResponse(
        UnidadeResumoDto unidade,
        List<CompetenciaMapaDto> competencias,
        List<AtividadeMapaDto> atividadesSemCompetencia,
        String sugestoes
) {
}
