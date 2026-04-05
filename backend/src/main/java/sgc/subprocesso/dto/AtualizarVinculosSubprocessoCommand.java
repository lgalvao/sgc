package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

/**
 * Command interno para atualização dos vínculos principais do subprocesso.
 */
@Builder
public record AtualizarVinculosSubprocessoCommand(
        @Nullable Long codUnidade,
        @Nullable Long codMapa
) {
}
