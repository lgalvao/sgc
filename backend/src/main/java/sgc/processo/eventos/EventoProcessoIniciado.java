package sgc.processo.eventos;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Evento de domínio publicado quando um processo é iniciado.
 *
 * <p>Carrega as informações essenciais para que outros módulos (como 'alerta' e 'notificacao')
 * possam reagir ao início de um processo.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoProcessoIniciado {
    /** O código do processo que foi iniciado. */
    private Long codProcesso;

    /** O tipo do processo (ex: "MAPEAMENTO", "REVISAO"). */
    private String tipo;

    /** A data e hora exatas do início. */
    private LocalDateTime dataHoraInicio;

    /** A lista de IDs das unidades que participam do processo. */
    private List<Long> codUnidades;
}
