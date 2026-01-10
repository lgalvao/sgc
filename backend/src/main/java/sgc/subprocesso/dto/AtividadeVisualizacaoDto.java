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
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private String descricao;
    private List<ConhecimentoVisualizacaoDto> conhecimentos;
}
