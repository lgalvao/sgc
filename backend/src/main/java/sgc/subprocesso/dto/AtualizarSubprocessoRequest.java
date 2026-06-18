package sgc.subprocesso.dto;

import lombok.*;

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
        Long codUnidade,
        Long codMapa,
        LocalDateTime dataLimiteEtapa1,
        LocalDateTime dataFimEtapa1,
        LocalDateTime dataLimiteEtapa2,
        LocalDateTime dataFimEtapa2) {
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
                .dataLimiteEtapa1(Optional.of(dataLimiteEtapa1))
                .dataFimEtapa1(Optional.of(dataFimEtapa1))
                .dataLimiteEtapa2(Optional.of(dataLimiteEtapa2))
                .dataFimEtapa2(Optional.of(dataFimEtapa2))
                .build();
    }
}
