package sgc.processo.eventos;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Sinaliza que todos os subprocessos foram homologados, os mapas foram tornados
 * vigentes e as notificações de conclusão foram enviadas.
 */
@Getter
@Builder
public class EventoProcessoFinalizado {
    private final Long codProcesso;
    private final LocalDateTime dataHoraFinalizacao;
}
