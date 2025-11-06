package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import sgc.comum.json.SanitizeHtml;

import java.time.LocalDateTime;

public record SubmeterMapaAjustadoReq(
    @NotBlank(message = "O campo 'observacoes' é obrigatório.")
    @SanitizeHtml
    String observacoes,

    @NotNull(message = "O campo 'dataLimiteEtapa2' é obrigatório.")
    @Future(message = "A data limite da etapa 2 deve ser uma data futura.")
    LocalDateTime dataLimiteEtapa2
) {}