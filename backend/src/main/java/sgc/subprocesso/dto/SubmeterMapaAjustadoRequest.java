package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;

/**
 * DTO de requisição para submeter mapa ajustado.
 */
@Getter
@Builder
@AllArgsConstructor
public class SubmeterMapaAjustadoRequest {
    @NotBlank(message = "O campo 'observacoes' é obrigatório.")
    @SanitizarHtml
    private final String observacoes;

    @NotNull(message = "O campo 'dataLimiteEtapa2' é obrigatório.")
    @Future(message = "A data limite da etapa 2 deve ser uma data futura.")
    private final LocalDateTime dataLimiteEtapa2;
}
