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
    private final Long codProcesso;
    private final String tipo;
    private final LocalDateTime dataHoraInicio;
    private final List<Long> codUnidades;
}
