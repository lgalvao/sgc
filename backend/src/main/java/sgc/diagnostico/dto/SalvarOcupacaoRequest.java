package sgc.diagnostico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sgc.diagnostico.model.SituacaoCapacitacao;

/**
 * Request para salvar ocupação crítica.
 * Conforme CDU-07 do DRAFT-Diagnostico.md.
 */
public record SalvarOcupacaoRequest(
        @NotBlank(message = "Título do servidor é obrigatório")
        String servidorTitulo,

        @NotNull(message = "Código da competência é obrigatório")
        Long competenciaCodigo,

        @NotNull(message = "Situação de capacitação é obrigatória")
        SituacaoCapacitacao situacao
) {
}
