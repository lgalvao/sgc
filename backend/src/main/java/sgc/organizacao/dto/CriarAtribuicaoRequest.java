package sgc.organizacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import sgc.comum.Mensagens;
import sgc.comum.model.TituloEleitoral;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDate;

public record CriarAtribuicaoRequest(
        @TituloEleitoral
        String tituloEleitoralUsuario,

        @NotNull(message = Mensagens.DATA_INICIO_OBRIGATORIA)
        LocalDate dataInicio,

        @NotNull(message = Mensagens.DATA_TERMINO_OBRIGATORIA)
        LocalDate dataTermino,

        @SanitizarHtml
        @NotBlank(message = Mensagens.JUSTIFICATIVA_OBRIGATORIA)
        @Size(max = 500, message = Mensagens.JUSTIFICATIVA_MAX)
        String justificativa) {
}
