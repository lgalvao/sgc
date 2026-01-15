package sgc.subprocesso.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para atividade no contexto de ajustes do mapa. CDU-16 item 4 e 5
 * 
 * <p>Usado como parte de {@link CompetenciaAjusteDto}.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class AtividadeAjusteDto {
    private final Long codAtividade;
    private final String nome;
    private final List<ConhecimentoAjusteDto> conhecimentos;
}
