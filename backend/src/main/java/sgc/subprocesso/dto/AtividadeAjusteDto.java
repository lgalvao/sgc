package sgc.subprocesso.dto;

import lombok.Builder;

import java.util.List;

/**
 * DTO para atividade no contexto de ajustes do mapa. CDU-16 item 4 e 5
 *
 * <p>
 * Usado como parte de {@link CompetenciaAjusteDto}.
 */
@Builder
public record AtividadeAjusteDto(
        Long codAtividade,
        String nome,
        List<ConhecimentoAjusteDto> conhecimentos) {
}
