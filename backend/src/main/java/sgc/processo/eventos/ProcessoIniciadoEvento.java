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
 * @param idProcesso O ID do processo que foi iniciado.
 * @param tipo O tipo do processo (ex: "MAPEAMENTO", "REVISAO").
 * @param dataHoraInicio A data e hora exatas do início.
 * @param idsUnidades A lista de IDs das unidades que participam do processo.
 */
public record ProcessoIniciadoEvento(
    Long idProcesso,
    String tipo,
    LocalDateTime dataHoraInicio,
    List<Long> idsUnidades
) {
    public ProcessoIniciadoEvento {
        idsUnidades = new ArrayList<>(idsUnidades);
    }

    @Override
    public List<Long> idsUnidades() {
        return new ArrayList<>(idsUnidades);
    }
}