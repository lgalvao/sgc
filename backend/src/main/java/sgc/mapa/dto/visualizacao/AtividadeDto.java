package sgc.mapa.dto.visualizacao;

import java.util.List;

import lombok.Builder;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        Long codigo,
        String descricao,
        List<ConhecimentoDto> conhecimentos) {
}
