package sgc.mapa.dto.visualizacao;

import java.util.List;

import lombok.Builder;

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
