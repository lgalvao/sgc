package sgc.mapa.dto.visualizacao;

import java.util.List;

public record MapaVisualizacaoDto(
        UnidadeDto unidade,
        List<CompetenciaDto> competencias
) {
    public record UnidadeDto(
            Long codigo,
            String sigla,
            String nome
    ) {
    }
}
