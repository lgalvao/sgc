package sgc.organizacao.model;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

@Builder
public record ResponsabilidadeUnidadeLeitura(
        Long unidadeCodigo,
        String usuarioTitulo,
        @Nullable String tituloTitular,
        @Nullable String tipo,
        @Nullable LocalDateTime dataInicio,
        @Nullable LocalDateTime dataFim
) {
}
