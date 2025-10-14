package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
@Getter
@Builder
public class ConhecimentoAjusteDto {
    @NotNull private final Long conhecimentoId;
    @NotBlank private final String nome;
    @NotNull private final boolean incluido;
}