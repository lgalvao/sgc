package sgc.mapa.dto;

import java.util.List;

public record CompetenciaMapaDto(
        Long codigo,
        String descricao,
        List<AtividadeMapaDto> atividades) {
}
