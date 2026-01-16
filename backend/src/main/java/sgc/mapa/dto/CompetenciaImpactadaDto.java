package sgc.mapa.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import sgc.mapa.model.TipoImpactoCompetencia;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada pelas mudanças nas atividades durante a
 * revisão do cadastro.
 *
 * <p>CDU-12 - Verificar impactos no mapa de competências
 */
@Getter
@Builder
@AllArgsConstructor
public class CompetenciaImpactadaDto {

    /**
     * O código da competência.
     */
    private final Long codigo;

    /**
     * A descrição da competência.
     */
    private final String descricao;

    /**
     * Lista com as descrições das atividades que causaram o impacto.
     */
    private final List<String> atividadesAfetadas; // Descrições das atividades que causaram impacto

    /**
     * Os tipos de impacto sofridos pela competência.
     * Uma competência pode ser afetada por múltiplos tipos de mudança (ex: atividade removida E alterada).
     */
    private final List<TipoImpactoCompetencia> tiposImpacto;
}
