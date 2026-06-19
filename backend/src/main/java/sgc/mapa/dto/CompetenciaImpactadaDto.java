package sgc.mapa.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO que representa uma competência que foi impactada por mudanças nas atividades na revisão.
 */
@Builder
public record CompetenciaImpactadaDto(
        Long codigo,
        String descricao,
        List<String> atividadesAfetadas,
        List<String> tiposImpacto) {
}
