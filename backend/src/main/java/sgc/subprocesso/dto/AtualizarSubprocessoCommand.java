package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

/**
 * Command interno para atualização de subprocesso.
 */
@Builder
public record AtualizarSubprocessoCommand(
        @Nullable AtualizarVinculosSubprocessoCommand vinculos,
        @Nullable AtualizarPrazosSubprocessoCommand prazos) {
}
