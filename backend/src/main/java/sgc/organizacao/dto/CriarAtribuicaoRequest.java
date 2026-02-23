package sgc.organizacao.dto;

import jakarta.validation.constraints.Size;
import sgc.comum.TituloEleitoral;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDate;

public record CriarAtribuicaoRequest(
        @TituloEleitoral
        String tituloEleitoralUsuario,

        LocalDate dataInicio,
        LocalDate dataTermino,

        @SanitizarHtml
        @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")
        String justificativa) {
}
