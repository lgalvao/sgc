package sgc.mapa.dto.visualizacao;

import java.util.ArrayList;
import java.util.List;

public record MapaVisualizacaoDto(
    UnidadeDto unidade,
    List<CompetenciaDto> competencias
) {
    public MapaVisualizacaoDto {
        competencias = new ArrayList<>(competencias);
    }

    @Override
    public List<CompetenciaDto> competencias() {
        return new ArrayList<>(competencias);
    }

    public record UnidadeDto(
        Long codigo,
        String sigla,
        String nome
    ) {}
}