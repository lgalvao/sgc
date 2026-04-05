package sgc.subprocesso.dto;

import lombok.*;

/**
 * Command interno para atualização de subprocesso.
 */
@Builder
public record AtualizarSubprocessoCommand(
        AtualizarVinculosSubprocessoCommand vinculos,
        AtualizarPrazosSubprocessoCommand prazos
) {
    public AtualizarSubprocessoCommand {
        if (vinculos == null) {
            vinculos = AtualizarVinculosSubprocessoCommand.builder().build();
        }
        if (prazos == null) {
            prazos = AtualizarPrazosSubprocessoCommand.builder().build();
        }
    }
}
