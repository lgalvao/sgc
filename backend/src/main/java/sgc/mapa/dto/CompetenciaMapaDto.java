package sgc.mapa.dto;

import sgc.mapa.model.*;

import java.util.*;

public record CompetenciaMapaDto(
        Long codigo,
        String descricao,
        List<AtividadeMapaDto> atividades) {

    public static CompetenciaMapaDto fromEntity(Competencia competencia) {
        List<AtividadeMapaDto> atividades = competencia.getAtividades().stream()
                .map(AtividadeMapaDto::fromEntity)
                .toList();

        return new CompetenciaMapaDto(
                competencia.getCodigo(),
                competencia.getDescricao(),
                atividades);
    }
}
