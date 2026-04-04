package sgc.organizacao.model;

import org.jspecify.annotations.*;

public record UnidadeHierarquiaLeitura(
        Long codigo,
        String nome,
        String sigla,
        @Nullable String tituloTitular,
        TipoUnidade tipo,
        SituacaoUnidade situacao,
        @Nullable Long unidadeSuperiorCodigo
) {
}
