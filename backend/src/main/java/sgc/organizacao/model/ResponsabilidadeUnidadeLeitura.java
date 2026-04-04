package sgc.organizacao.model;

import org.jspecify.annotations.*;

import java.time.*;

public record ResponsabilidadeUnidadeLeitura(
        Long unidadeCodigo,
        String usuarioTitulo,
        @Nullable String tituloTitular,
        @Nullable String tipo,
        @Nullable LocalDateTime dataInicio,
        @Nullable LocalDateTime dataFim
) {
}
