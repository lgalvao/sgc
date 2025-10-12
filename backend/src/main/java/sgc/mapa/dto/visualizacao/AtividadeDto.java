package sgc.mapa.dto.visualizacao;

import java.util.List;

public record AtividadeDto(
    Long id,
    String descricao,
    List<ConhecimentoDto> conhecimentos
) {}