package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 *
 * <p>
 * Usado como parte de {@link AtividadeAjusteDto}.
 */
@Builder
public record ConhecimentoAjusteDto(
        @Nullable Long conhecimentoCodigo,
        String nome,
        boolean incluido) {
}
