package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DTO para conhecimento no contexto de ajustes do mapa.
 * 
 * <p>Usado como parte de {@link AtividadeAjusteDto}.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class ConhecimentoAjusteDto {
    private final Long conhecimentoCodigo;
    private final String nome;
    private final boolean incluido;
}
