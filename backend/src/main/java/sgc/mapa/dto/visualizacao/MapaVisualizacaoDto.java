package sgc.mapa.dto.visualizacao;

import java.util.List;

public record MapaVisualizacaoDto(
    UnidadeDto unidade,
    List<CompetenciaDto> competencias
) {
    public record UnidadeDto(
        Long id,
        String sigla,
        String nome
    ) {}
}