package sgc.mapa.dto.visualizacao;

import lombok.Builder;

import java.util.List;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        Long codigo,
        String descricao,
        List<ConhecimentoDto> conhecimentos) {
}
