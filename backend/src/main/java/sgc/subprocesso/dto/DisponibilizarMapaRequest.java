package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.*;
import sgc.seguranca.sanitizacao.*;

import java.time.*;

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
