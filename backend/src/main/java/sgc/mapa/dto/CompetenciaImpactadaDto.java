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
    /** O código da competência. */
    private Long codigo;

    /** A descrição da competência. */
    private String descricao;

    /** Lista com as descrições das atividades que causaram o impacto. */
    private List<String> atividadesAfetadas; // Descrições das atividades que causaram impacto

    /** O tipo de impacto sofrido pela competência (e.g., nova atividade associada). */
    private TipoImpactoCompetencia
            tipoImpacto; // NOVA_ATIVIDADE, ATIVIDADE_REMOVIDA, ATIVIDADE_ALTERADA
}
