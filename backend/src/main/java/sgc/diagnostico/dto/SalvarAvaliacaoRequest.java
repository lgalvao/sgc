package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotNull;
import sgc.diagnostico.model.NivelAvaliacao;

/**
 * Request para salvar avaliação de competência por servidor.
 * Conforme CDU-02 do DRAFT-Diagnostico.md.
 */
public record SalvarAvaliacaoRequest(
        @NotNull(message = "Código da competência é obrigatório")
        Long competenciaCodigo,

        @NotNull(message = "Importância é obrigatória")
        NivelAvaliacao importancia,

        @NotNull(message = "Domínio é obrigatório")
        NivelAvaliacao dominio,

        String observacoes
) {
}
