package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * Requisição para disponibilizar mapa.
 */
@Builder
public record DisponibilizarMapaRequest(
        @NotNull(message = "A data limite para validação é obrigatória.") @Future(message = "A data limite para validação deve ser uma data futura.") LocalDate dataLimite,
        String observacoes) {
}
