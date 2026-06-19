package sgc.organizacao.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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
