package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnviarLembreteRequestuest {
    @NotNull(message = "O código da unidade é obrigatório")
    private Long unidadeCodigo;
}
