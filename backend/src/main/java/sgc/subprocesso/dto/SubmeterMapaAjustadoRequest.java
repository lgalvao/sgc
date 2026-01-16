package sgc.subprocesso.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sgc.seguranca.sanitizacao.SanitizarHtml;

import java.time.LocalDateTime;

/**
 * DTO de requisição para submeter mapa ajustado.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmeterMapaAjustadoRequest {
    @NotBlank(message = "O campo 'observacoes' é obrigatório.")
    @SanitizarHtml
    private String observacoes;

    @NotNull(message = "O campo 'dataLimiteEtapa2' é obrigatório.")
    @Future(message = "A data limite da etapa 2 deve ser uma data futura.")
    private LocalDateTime dataLimiteEtapa2;
}
