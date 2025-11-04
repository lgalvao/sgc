package sgc.mapa.dto.visualizacao;

import java.util.List;

// TODO essa classe e todo esse pacote estao me parecendo redundantes. Se nao for redundante, mude o nome e documente.
public record AtividadeDto(
        Long codigo,
        String descricao,
        List<ConhecimentoDto> conhecimentos
) {
}