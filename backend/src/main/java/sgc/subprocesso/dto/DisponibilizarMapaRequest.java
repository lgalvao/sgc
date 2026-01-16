package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO de requisição para disponibilizar mapa.
 */
@Getter
@Builder
@AllArgsConstructor
public class DisponibilizarMapaRequest {

    @NotNull(message = "A data limite para validação é obrigatória.")
    @Future(message = "A data limite para validação deve ser uma data futura.")
    private final LocalDate dataLimite;

    private final String observacoes;
}
