package sgc.organizacao.dto;

import jakarta.validation.constraints.*;
import sgc.comum.MsgValidacao;
import sgc.comum.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;

public record CriarAtribuicaoRequest(
        @TituloEleitoral
        String tituloEleitoralUsuario,

        LocalDate dataInicio,
        LocalDate dataTermino,

        @SanitizarHtml
        @Size(max = 500, message = MsgValidacao.JUSTIFICATIVA_MAX)
        String justificativa) {
}
