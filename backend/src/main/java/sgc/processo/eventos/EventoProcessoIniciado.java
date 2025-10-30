package sgc.processo.eventos;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Evento de domínio publicado quando um processo é iniciado.
 * <p>
 * Carrega as informações essenciais para que outros módulos (como 'alerta'
 * e 'notificacao') possam reagir ao início de um processo.
 *
 * @param codProcesso O código do processo que foi iniciado.
 * @param tipo O tipo do processo (ex: "MAPEAMENTO", "REVISAO").
 * @param dataHoraInicio A data e hora exatas do início.
 * @param codUnidades A lista de IDs das unidades que participam do processo.
 */
public record EventoProcessoIniciado(
    Long codProcesso,
    String tipo,
    LocalDateTime dataHoraInicio,
    List<Long> codUnidades
) {
    public EventoProcessoIniciado {
        codUnidades = new ArrayList<>(codUnidades);
    }

    @Override
    public List<Long> codUnidades() {
        return new ArrayList<>(codUnidades);
    }
}