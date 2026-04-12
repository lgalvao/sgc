package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;
import java.util.*;

/**
 * DTO de requisição para atualizar um subprocesso existente.
 *
 * <p>
 * Usado exclusivamente como entrada de API para o endpoint de atualização.
 * Diferente da criação, não requer codProcesso pois o subprocesso já existe.
 */
@Builder
public record AtualizarSubprocessoRequest(
        @Nullable Long codUnidade,
        @Nullable Long codMapa,
        @Nullable LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2) {
    public AtualizarSubprocessoCommand paraCommand() {
        return AtualizarSubprocessoCommand.builder()
                .vinculos(paraVinculosCommand())
                .prazos(paraPrazosCommand())
                .build();
    }

    public AtualizarVinculosSubprocessoCommand paraVinculosCommand() {
        return AtualizarVinculosSubprocessoCommand.builder()
                .codUnidade(codUnidade)
                .codMapa(codMapa)
                .build();
    }

    public AtualizarPrazosSubprocessoCommand paraPrazosCommand() {
        return AtualizarPrazosSubprocessoCommand.builder()
                .dataLimiteEtapa1(Optional.ofNullable(dataLimiteEtapa1))
                .dataFimEtapa1(Optional.ofNullable(dataFimEtapa1))
                .dataLimiteEtapa2(Optional.ofNullable(dataLimiteEtapa2))
                .dataFimEtapa2(Optional.ofNullable(dataFimEtapa2))
                .build();
    }
}
