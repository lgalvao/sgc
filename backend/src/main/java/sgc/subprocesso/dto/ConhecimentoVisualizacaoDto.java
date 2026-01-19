package sgc.subprocesso.dto;

import lombok.*;

/**
 * DTO para visualização de conhecimento.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em testes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConhecimentoVisualizacaoDto {
    private Long codigo;
    private String descricao;
}
