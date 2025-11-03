package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * DTO para atividade no contexto de ajustes do mapa.
 * CDU-16 item 4 e 5
 */
@Getter
@Builder
public class AtividadeAjusteDto {
    @NotNull private final Long codAtividade;
    @NotBlank private final String nome;
    @NotNull @Valid private final List<ConhecimentoAjusteDto> conhecimentos;
}