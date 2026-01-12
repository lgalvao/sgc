package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para visualização de atividade com seus conhecimentos.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeVisualizacaoDto {

    private Long codigo;
    private String descricao;
    private List<ConhecimentoVisualizacaoDto> conhecimentos;
}
