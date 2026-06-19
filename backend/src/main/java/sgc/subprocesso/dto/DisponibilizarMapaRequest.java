package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import sgc.comum.Mensagens;
import sgc.seguranca.sanitizacao.SanitizarHtmlFormatado;

import java.time.LocalDate;

/**
 * Requisição para disponibilizar mapa.
 */
@Builder
public record DisponibilizarMapaRequest(
        @NotNull(message = Mensagens.DATA_LIMITE_VALIDACAO_OBRIGATORIA)
        @Future(message = Mensagens.DATA_LIMITE_VALIDACAO_FUTURA) LocalDate dataLimite,
        @Size(max = 500, message = Mensagens.OBSERVACOES_MAX_500)
        @SanitizarHtmlFormatado
        String observacoes) {
}
