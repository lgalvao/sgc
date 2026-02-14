package sgc.analise.dto;

import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.comum.validacao.TituloEleitoral;
import jakarta.validation.constraints.NotNull;

@Builder
public record CriarAnaliseRequest(
        @TituloEleitoral
        String tituloUsuario,

        @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
        String observacoes,

        @NotNull(message = "A sigla da unidade é obrigatória")
        @Size(max = 20, message = "Sigla da unidade deve ter no máximo 20 caracteres")
        String siglaUnidade,

        @Size(max = 200, message = "Motivo deve ter no máximo 200 caracteres")
        String motivo
) {
}
