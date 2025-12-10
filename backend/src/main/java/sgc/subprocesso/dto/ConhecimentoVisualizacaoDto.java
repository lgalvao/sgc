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
    private Long codigo;
    private String descricao;
}
