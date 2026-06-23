package sgc.organizacao.dto;

import jakarta.validation.constraints.*;
import sgc.comum.*;
import sgc.comum.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;

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
