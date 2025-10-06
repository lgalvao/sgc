package sgc.mapa.dto;

import java.util.List;

/**
 * DTO que representa uma atividade que sofreu alteração durante
 * a revisão do cadastro.
 * <p>
 * CDU-12 - Verificar impactos no mapa de competências
 */
public record AtividadeImpactadaDto(
    Long codigo,
    String descricao,
    String tipoImpacto,  // INSERIDA, REMOVIDA, ALTERADA
    String descricaoAnterior,  // Para ALTERADA (null para outros tipos)
    List<String> competenciasVinculadas  // Nomes das competências vinculadas
) {}