package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Command interno para atualização dos vínculos principais do subprocesso.
 */
@Builder
public record AtualizarVinculosSubprocessoCommand(
        @Nullable Long codUnidade,
        @Nullable Long codMapa
) {
}
