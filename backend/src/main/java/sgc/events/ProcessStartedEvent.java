package sgc.events;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evento publicado quando um processo é iniciado (MAPEAMENTO/REVISAO/DIAGNOSTICO).
 * Contém payload leve com id do processo, tipo, timestamp e lista de unidades participantes.
 */
public record ProcessStartedEvent(Long processoId, String tipo, LocalDateTime timestamp, List<Long> unidades) {
}