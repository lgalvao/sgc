package sgc.processo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO de requisição para enviar lembrete a uma unidade.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para deserialização Jackson.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnviarLembreteRequest {
    @NotNull(message = "O código da unidade é obrigatório")
    private Long unidadeCodigo;
}
