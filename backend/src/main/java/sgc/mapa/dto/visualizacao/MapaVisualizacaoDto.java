package sgc.mapa.dto.visualizacao;

import java.util.List;

import java.util.ArrayList;

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
        Long id,
        String sigla,
        String nome
    ) {}
}