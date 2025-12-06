package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ConhecimentoAjusteDto {
    @NotNull private final Long conhecimentoId;
    @NotBlank private final String nome;
    @NotNull private final boolean incluido;
}
