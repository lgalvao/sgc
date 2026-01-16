package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ReabrirProcessoRequest {
    @NotBlank(message = "A justificativa é obrigatória")
    private final String justificativa;
}
