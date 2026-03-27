package sgc.mapa.dto;

import lombok.*;
import org.jspecify.annotations.*;
import sgc.mapa.model.*;

import java.util.*;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        @Nullable Long codigo,
        String descricao,
        List<ConhecimentoResumoDto> conhecimentos) {

    public static AtividadeDto fromEntity(Atividade atividade) {
        return AtividadeDto.builder()
                .codigo(atividade.getCodigo())
                .descricao(atividade.getDescricao())
                .conhecimentos(atividade.getConhecimentos().stream()
                        .map(ConhecimentoResumoDto::fromEntity)
                        .toList())
                .build();
    }
}

