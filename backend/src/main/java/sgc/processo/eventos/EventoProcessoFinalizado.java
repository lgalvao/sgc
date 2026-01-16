package sgc.processo.eventos;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Evento de domínio publicado quando um Processo é finalizado.
 *
 * <p>Este evento sinaliza que todos os subprocessos foram homologados, os mapas foram tornados
 * vigentes e as notificações de conclusão foram enviadas. Pode ser usado para acionar lógicas de
 * pós-processamento, como arquivamento ou geração de relatórios consolidados.
 */
@Getter
@Builder
public class EventoProcessoFinalizado {
    private final Long codProcesso;
    private final LocalDateTime dataHoraFinalizacao;
}
