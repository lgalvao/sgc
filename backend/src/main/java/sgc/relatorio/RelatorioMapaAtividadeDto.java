package sgc.relatorio;

import java.util.List;

public record RelatorioMapaAtividadeDto(
        Long codigo,
        String descricao,
        List<RelatorioMapaConhecimentoDto> conhecimentos
) {
}
