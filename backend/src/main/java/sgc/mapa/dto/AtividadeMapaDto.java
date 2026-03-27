package sgc.mapa.dto;

import sgc.mapa.model.*;

import java.util.*;

public record AtividadeMapaDto(
        Long codigo,
        String descricao,
        List<ConhecimentoResumoDto> conhecimentos) {

    public static AtividadeMapaDto fromEntity(Atividade atividade) {
        List<ConhecimentoResumoDto> conhecimentos = atividade.getConhecimentos().stream()
                .map(ConhecimentoResumoDto::fromEntity)
                .toList();

        return new AtividadeMapaDto(
                atividade.getCodigo(),
                atividade.getDescricao(),
                conhecimentos);
    }
}
