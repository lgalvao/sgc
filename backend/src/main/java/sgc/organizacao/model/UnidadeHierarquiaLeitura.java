package sgc.organizacao.model;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
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
