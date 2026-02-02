package sgc.mapa.dto.visualizacao;

import lombok.Builder;

import java.util.List;

/**
 * DTO para visualização de mapa completo.
 */
@Builder
public record MapaVisualizacaoDto(
        UnidadeDto unidade,
        List<CompetenciaDto> competencias,
        List<AtividadeDto> atividadesSemCompetencia,
        String sugestoes) {

    @Builder
    public record UnidadeDto(
            Long codigo,
            String sigla,
            String nome) {
    }
}
