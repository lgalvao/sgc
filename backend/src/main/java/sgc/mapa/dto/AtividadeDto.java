package sgc.mapa.dto;

import lombok.Builder;
import sgc.mapa.model.Conhecimento;

import java.util.List;

/**
 * DTO para visualização de atividade.
 */
@Builder
public record AtividadeDto(
        Long codigo,
        String descricao,
        List<Conhecimento> conhecimentos) {
}
