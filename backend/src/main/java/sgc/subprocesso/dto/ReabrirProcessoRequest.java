package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

@Getter
@Builder
@AllArgsConstructor
public class ReabrirProcessoRequest {
    @NotBlank(message = "A justificativa é obrigatória")
    @SanitizarHtml
    private final String justificativa;
}
