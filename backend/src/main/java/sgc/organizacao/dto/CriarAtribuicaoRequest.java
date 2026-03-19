package sgc.organizacao.dto;

import jakarta.validation.constraints.*;
import sgc.comum.SgcMensagens;
import sgc.comum.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;

public record CriarAtribuicaoRequest(
        @TituloEleitoral
        String tituloEleitoralUsuario,

        LocalDate dataInicio,
        LocalDate dataTermino,

        @SanitizarHtml
        @Size(max = 500, message = SgcMensagens.JUSTIFICATIVA_MAX)
        String justificativa) {
}
