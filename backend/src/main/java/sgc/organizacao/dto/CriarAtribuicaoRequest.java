package sgc.organizacao.dto;

import jakarta.validation.constraints.*;
import sgc.comum.model.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;

public record CriarAtribuicaoRequest(
        @TituloEleitoral
        String tituloEleitoralUsuario,

        LocalDate dataInicio,
        LocalDate dataTermino,

        @SanitizarHtml
        @Size(max = 500, message = "A justificativa deve ter no máximo 500 caracteres")
        String justificativa) {
}
