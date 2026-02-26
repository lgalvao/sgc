package sgc.subprocesso.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.*;

/**
 * Requisição para disponibilizar mapa.
 */
@Builder
public record DisponibilizarMapaRequest(
        @NotNull(message = "A data limite para validação é obrigatória.") @Future(message = "A data limite para validação deve ser uma data futura.") LocalDate dataLimite,
        String observacoes) {
}
