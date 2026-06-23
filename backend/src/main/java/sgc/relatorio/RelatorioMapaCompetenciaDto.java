package sgc.relatorio;

import java.util.*;

public record RelatorioMapaCompetenciaDto(
        Long codigo,
        String descricao,
        List<RelatorioMapaAtividadeDto> atividades
) {
    public RelatorioMapaCompetenciaDto {
        atividades = List.copyOf(atividades);
    }
}
