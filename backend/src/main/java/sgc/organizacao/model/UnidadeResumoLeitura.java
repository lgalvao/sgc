package sgc.organizacao.model;

public record UnidadeResumoLeitura(
        Long codigo,
        String nome,
        String sigla,
        TipoUnidade tipo
) {
}
