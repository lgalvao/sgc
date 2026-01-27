package sgc.mapa.dto.visualizacao;

import lombok.Builder;

/**
 * DTO para visualização de conhecimento.
 */
@Builder
public record ConhecimentoDto(
        Long codigo,
        String descricao) {
}
