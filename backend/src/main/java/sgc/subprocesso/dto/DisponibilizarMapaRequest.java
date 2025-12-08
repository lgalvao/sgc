package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.*;

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
