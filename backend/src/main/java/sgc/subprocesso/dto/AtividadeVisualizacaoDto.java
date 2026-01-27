package sgc.subprocesso.dto;

import java.util.List;

import lombok.Builder;

/**
 * DTO para visualização de atividade com seus conhecimentos.
 */
@Builder
public record AtividadeVisualizacaoDto(
        Long codigo,
        String descricao,
        List<ConhecimentoVisualizacaoDto> conhecimentos) {
}
