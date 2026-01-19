package sgc.mapa.dto.visualizacao;

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
public class ConhecimentoDto {

    private Long codigo;
    private String descricao;
}
