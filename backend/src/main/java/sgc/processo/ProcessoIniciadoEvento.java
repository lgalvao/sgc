package sgc.processo;

import java.time.LocalDateTime;
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
) {}