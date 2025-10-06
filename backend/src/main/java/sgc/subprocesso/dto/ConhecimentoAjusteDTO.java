package sgc.subprocesso.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
public record ConhecimentoAjusteDTO(
    @NotNull Long conhecimentoId,
    @NotBlank String nome,
    @NotNull boolean incluido
) {}