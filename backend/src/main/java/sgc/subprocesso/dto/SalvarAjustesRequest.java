package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request para salvar ajustes no mapa de competências. CDU-16 item 5
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalvarAjustesRequest {
    /**
     * A lista de competências com os ajustes a serem salvos.
     */
    @NotNull(message = "A lista de competências é obrigatória")
    @Valid
    private List<CompetenciaAjusteDto> competencias;
}
