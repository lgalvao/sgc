package sgc.organizacao.model;

import lombok.*;

@Builder
public record UnidadeResumoLeitura(
        Long codigo,
        String nome,
        String sigla,
        TipoUnidade tipo
) {
}
