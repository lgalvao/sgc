package sgc.mapa.dto.visualizacao;

import java.util.List;

public record AtividadeDto(
        Long codigo,
        String descricao,
        List<ConhecimentoDto> conhecimentos
) {
}
