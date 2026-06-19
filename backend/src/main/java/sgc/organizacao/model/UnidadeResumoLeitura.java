package sgc.organizacao.model;

import lombok.Builder;

@Builder
public record UnidadeResumoLeitura(
        Long codigo,
        String nome,
        String sigla,
        TipoUnidade tipo
) {
}
