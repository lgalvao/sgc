package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import sgc.comum.SgcMensagens;

import java.time.*;

/**
 * Requisição para disponibilizar mapa.
 */
@Builder
public record DisponibilizarMapaRequest(
        @NotNull(message = SgcMensagens.DATA_LIMITE_VALIDACAO_OBRIGATORIA) @Future(message = SgcMensagens.DATA_LIMITE_VALIDACAO_FUTURA) LocalDate dataLimite,
        String observacoes) {
}
