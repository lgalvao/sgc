package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças nas atividades durante a
 * revisão do cadastro.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetenciaImpactadaDto {
    /**
     * O código da competência.
     */
    private Long codigo;

    /**
     * A descrição da competência.
     */
    private String descricao;

    /**
     * Lista com as descrições das atividades que causaram o impacto.
     */
    private List<String> atividadesAfetadas; // Descrições das atividades que causaram impacto

    /**
     * Os tipos de impacto sofridos pela competência.
     * Uma competência pode ser afetada por múltiplos tipos de mudança (ex: atividade removida E alterada).
     */
    private List<TipoImpactoCompetencia> tiposImpacto;
}
