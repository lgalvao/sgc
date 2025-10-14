package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO para competência no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
@Getter
@Builder
public class CompetenciaAjusteDto {
    @NotNull private final Long competenciaId;
    @NotBlank private final String nome;
    @NotNull @Valid private final List<AtividadeAjusteDto> atividades;
}