package sgc.mapa.dto;

import lombok.*;
import sgc.organizacao.dto.*;

import java.util.*;

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
