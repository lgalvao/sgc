package sgc.subprocesso.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO para visualização de atividade com seus conhecimentos. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeVisualizacaoDto {
    private Long codigo;
    private String descricao;
    private List<ConhecimentoVisualizacaoDto> conhecimentos;
}
