package sgc.organizacao.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

@Builder
public record ResponsabilidadeUnidadeResumoLeitura(
        Long unidadeCodigo,
        String responsavelTitulo,
        @Nullable String responsavelNome,
        @Nullable String titularTitulo,
        @Nullable String titularNome
) {
}
