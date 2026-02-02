package sgc.mapa.dto.visualizacao;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para visualização de competência.
 *
 * <p>Mantido como class por necessitar de mutabilidade para adicionar atividades durante 
 * a construção no {@link sgc.mapa.service.MapaVisualizacaoService}.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaDto {

    private Long codigo;
    private String descricao;
    @Builder.Default
    private List<AtividadeDto> atividades = new ArrayList<>();
}
