package sgc.mapa.dto.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para visualização de competência.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em testes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaDto {

    private Long codigo;
    private String descricao;
    private List<AtividadeDto> atividades;
}
