package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReabrirProcessoReq {
    @NotBlank(message = "A justificativa é obrigatória")
    private String justificativa;
}
