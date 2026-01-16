package sgc.mapa.dto.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para visualização de mapa completo.
 * 
 * <p>Requer @NoArgsConstructor e @Setter para uso em testes.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaVisualizacaoDto {

    private UnidadeDto unidade;
    private List<CompetenciaDto> competencias;
    private List<AtividadeDto> atividadesSemCompetencia;
    private String sugestoes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnidadeDto {
        private Long codigo;
        private String sigla;
        private String nome;
    }
}
