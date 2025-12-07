package sgc.subprocesso.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request para salvar ajustes no mapa de competências. CDU-16 item 5 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalvarAjustesReq {
    /** A lista de competências com os ajustes a serem salvos. */
    @NotNull @Valid private List<CompetenciaAjusteDto> competencias;
}
