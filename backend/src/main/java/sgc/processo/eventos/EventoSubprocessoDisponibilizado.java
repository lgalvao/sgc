package sgc.processo.eventos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de domínio publicado quando um Subprocesso é disponibilizado para a próxima etapa do fluxo
 * (ex: disponibilizar cadastro, disponibilizar mapa).
 *
 * <p>Este evento é usado para notificar outros sistemas ou módulos, como o de alertas, que uma ação
 * foi concluída e uma nova etapa está pronta para começar.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoSubprocessoDisponibilizado {
    /** O código do subprocesso que foi disponibilizado. */
    private Long codSubprocesso;
}
