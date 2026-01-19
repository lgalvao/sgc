package sgc.subprocesso.dto;

import lombok.*;

import java.util.List;

/**
 * DTO para visualização de atividade com seus conhecimentos.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em testes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtividadeVisualizacaoDto {
    private Long codigo;
    private String descricao;
    private List<ConhecimentoVisualizacaoDto> conhecimentos;
}
