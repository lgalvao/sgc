package sgc.processo;

import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio publicado quando um Processo é finalizado com sucesso.
 * <p>
 * Este evento sinaliza que todos os subprocessos foram homologados, os mapas
 * foram tornados vigentes e as notificações de conclusão foram enviadas.
 * Pode ser usado para acionar lógicas de pós-processamento, como arquivamento
 * ou geração de relatórios consolidados.
 */
public class ProcessoFinalizadoEvento extends ApplicationEvent {
    private final Long idProcesso;

    public ProcessoFinalizadoEvento(Object source, Long idProcesso) {
        super(source);
        this.idProcesso = idProcesso;
    }

    public Long getIdProcesso() {
        return idProcesso;
    }
}