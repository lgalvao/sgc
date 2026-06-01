package sgc.mapa.dto;

import java.util.*;

public record CompetenciaMapaDto(
        Long codigo,
        String descricao,
        List<AtividadeMapaDto> atividades) {
}
