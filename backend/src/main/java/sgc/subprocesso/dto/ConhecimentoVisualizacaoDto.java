package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO para visualização de conhecimento.
 */
@Builder
public record ConhecimentoVisualizacaoDto(
        Long codigo,
        String descricao) {
}
