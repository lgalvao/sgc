package sgc.processo.eventos;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio publicado quando um Processo é finalizado.
 * <p>
 * Este evento sinaliza que todos os subprocessos foram homologados, os mapas
 * foram tornados vigentes e as notificações de conclusão foram enviadas.
 * Pode ser usado para acionar lógicas de pós-processamento, como arquivamento
 * ou geração de relatórios consolidados.
 */
@Getter
public class EventoProcessoFinalizado extends ApplicationEvent {
    private final Long codProcesso;

    public EventoProcessoFinalizado(Object source, Long codProcesso) {
        super(source);
        this.codProcesso = codProcesso;
    }

}