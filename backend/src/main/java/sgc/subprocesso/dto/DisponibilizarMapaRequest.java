package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO de requisição para disponibilizar mapa.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisponibilizarMapaRequest {

    @NotNull(message = "A data limite para validação é obrigatória.")
    @Future(message = "A data limite para validação deve ser uma data futura.")
    private LocalDate dataLimite;

    private String observacoes;
}
