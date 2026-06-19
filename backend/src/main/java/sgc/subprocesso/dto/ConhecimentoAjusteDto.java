package sgc.subprocesso.dto;

import lombok.Builder;
import org.jspecify.annotations.Nullable;

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
