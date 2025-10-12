package sgc.mapa.dto.visualizacao;

import java.util.List;

public record CompetenciaDto(
    Long id,
    String descricao,
    List<AtividadeDto> atividades
) {}