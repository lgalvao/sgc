package sgc.mapa.dto.visualizacao;

import java.util.List;

public record CompetenciaDto(
        Long codigo,
        String descricao,
        List<AtividadeDto> atividades
) {
}
