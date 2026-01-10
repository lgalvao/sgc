package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para atividade no contexto de ajustes do mapa. CDU-16 item 4 e 5
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AtividadeAjusteDto {
    @NotNull(message = "O código da atividade é obrigatório")
    private final Long codAtividade;
    @NotBlank(message = "O nome da atividade é obrigatório")
    private final String nome;
    @NotNull(message = "A lista de conhecimentos é obrigatória")
    @Valid
    private final List<ConhecimentoAjusteDto> conhecimentos;
}
