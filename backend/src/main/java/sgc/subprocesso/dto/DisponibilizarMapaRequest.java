package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.MsgValidacao;

import java.time.*;

/**
 * Requisição para disponibilizar mapa.
 */
@Builder
public record DisponibilizarMapaRequest(
        @NotNull(message = MsgValidacao.DATA_LIMITE_VALIDACAO_OBRIGATORIA) @Future(message = MsgValidacao.DATA_LIMITE_VALIDACAO_FUTURA) LocalDate dataLimite,
        String observacoes) {
}
