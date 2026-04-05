package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.time.*;

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
        @Nullable LocalDateTime dataLimiteEtapa1,
        @Nullable LocalDateTime dataFimEtapa1,
        @Nullable LocalDateTime dataLimiteEtapa2,
        @Nullable LocalDateTime dataFimEtapa2) {
    public AtualizarSubprocessoCommand paraCommand() {
        return new AtualizarSubprocessoCommand(
                codUnidade,
                codMapa,
                dataLimiteEtapa1,
                dataFimEtapa1,
                dataLimiteEtapa2,
                dataFimEtapa2
        );
    }
}
