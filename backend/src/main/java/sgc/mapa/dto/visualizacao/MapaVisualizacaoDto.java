package sgc.mapa.dto.visualizacao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapaVisualizacaoDto {
    private UnidadeDto unidade;
    private List<CompetenciaDto> competencias;
    private List<AtividadeDto> atividadesSemCompetencia;
    private String sugestoes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnidadeDto {
        private Long codigo;
        private String sigla;
        private String nome;
    }
}
