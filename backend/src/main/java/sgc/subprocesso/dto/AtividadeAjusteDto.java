package sgc.subprocesso.dto;

import lombok.*;
import org.jspecify.annotations.*;

import java.util.*;

/**
 * DTO para atividade no contexto de ajustes do mapa. CDU-16 item 4 e 5
 *
 * <p>
 * Usado como parte de {@link CompetenciaAjusteDto}.
 */
@Builder
public record AtividadeAjusteDto(
        @Nullable Long codAtividade,
        String nome,
        List<ConhecimentoAjusteDto> conhecimentos) {
}
