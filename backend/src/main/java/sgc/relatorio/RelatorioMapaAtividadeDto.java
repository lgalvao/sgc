package sgc.relatorio;

import java.util.*;

public record RelatorioMapaAtividadeDto(
        Long codigo,
        String descricao,
        List<RelatorioMapaConhecimentoDto> conhecimentos
) {
    public RelatorioMapaAtividadeDto {
        conhecimentos = List.copyOf(conhecimentos);
    }
}
