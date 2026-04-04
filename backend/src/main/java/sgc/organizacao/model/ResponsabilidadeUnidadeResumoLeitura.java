package sgc.organizacao.model;

import org.jspecify.annotations.*;

public record ResponsabilidadeUnidadeResumoLeitura(
        Long unidadeCodigo,
        String responsavelTitulo,
        @Nullable String responsavelNome,
        @Nullable String titularTitulo,
        @Nullable String titularNome
) {
}
