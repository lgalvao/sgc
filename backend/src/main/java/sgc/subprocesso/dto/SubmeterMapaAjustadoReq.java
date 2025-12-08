package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.comum.json.SanitizarHtml;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmeterMapaAjustadoReq {
    @NotBlank(message = "O campo 'observacoes' é obrigatório.")
    @SanitizarHtml
    private String observacoes;

    @NotNull(message = "O campo 'dataLimiteEtapa2' é obrigatório.")
    @Future(message = "A data limite da etapa 2 deve ser uma data futura.")
    private LocalDateTime dataLimiteEtapa2;
}
