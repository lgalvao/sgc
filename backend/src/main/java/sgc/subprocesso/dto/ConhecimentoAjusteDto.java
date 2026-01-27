package sgc.subprocesso.dto;

import lombok.Builder;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 *
 * <p>
 * Usado como parte de {@link AtividadeAjusteDto}.
 */
@Builder
public record ConhecimentoAjusteDto(
                Long conhecimentoCodigo,
                String nome,
                boolean incluido) {
}
