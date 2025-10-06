package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * DTO para competência no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
public record CompetenciaAjusteDTO(
    @NotNull Long competenciaId,
    @NotBlank String nome,
    @NotNull @Valid List<AtividadeAjusteDTO> atividades
) {}