package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para visualização de conhecimento.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConhecimentoVisualizacaoDto {
    @java.io.Serial
    private static final long serialVersionUID = 1L;
    private Long codigo;
    private String descricao;
}
