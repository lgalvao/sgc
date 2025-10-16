package sgc.mapa.dto.visualizacao;

import java.util.ArrayList;
import java.util.List;

public record CompetenciaDto(
    Long id,
    String descricao,
    List<AtividadeDto> atividades
) {
    public CompetenciaDto {
        atividades = new ArrayList<>(atividades);
    }

    @Override
    public List<AtividadeDto> atividades() {
        return new ArrayList<>(atividades);
    }
}