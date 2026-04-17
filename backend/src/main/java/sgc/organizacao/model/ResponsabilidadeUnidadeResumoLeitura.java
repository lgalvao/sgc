package sgc.organizacao.model;

import lombok.*;
import org.jspecify.annotations.*;

@Builder
public record ResponsabilidadeUnidadeResumoLeitura(
        Long unidadeCodigo,
        String responsavelTitulo,
        @Nullable String responsavelNome,
        @Nullable String titularTitulo,
        @Nullable String titularNome
) {
}
