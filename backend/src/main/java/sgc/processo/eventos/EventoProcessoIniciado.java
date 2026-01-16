package sgc.processo.eventos;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento de domínio publicado quando um processo é iniciado.
 *
 * <p>Carrega as informações essenciais para que outros módulos (como 'alerta' e 'notificacao')
 * possam reagir ao início de um processo.
 */
@Getter
@Builder
public class EventoProcessoIniciado {
    /**
     * O código do processo que foi iniciado.
     */
    private final Long codProcesso;

    /**
     * O tipo do processo (ex: "MAPEAMENTO", "REVISAO").
     */
    private final String tipo;

    /**
     * A data e hora exatas do início.
     */
    private final LocalDateTime dataHoraInicio;

    /**
     * A lista de IDs das unidades que participam do processo.
     */
    private final List<Long> codUnidades;
}
