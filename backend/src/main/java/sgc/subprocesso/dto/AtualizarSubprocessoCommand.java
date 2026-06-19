package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

/**
 * Command interno para atualização de subprocesso.
 */
@Builder
public record AtualizarSubprocessoCommand(
        @Nullable AtualizarVinculosSubprocessoCommand vinculos,
        @Nullable AtualizarPrazosSubprocessoCommand prazos) {
}
