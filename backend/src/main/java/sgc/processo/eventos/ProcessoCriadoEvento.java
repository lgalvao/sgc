package sgc.processo.eventos;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Evento de domínio publicado quando um novo Processo é persistido pela primeira vez.
 * <p>
 * Este evento é mais simples e normalmente usado para logging ou auditoria inicial.
 * O evento {@link ProcessoIniciadoEvento} é usado para as lógicas de negócio mais complexas
 * que ocorrem quando o processo efetivamente começa.
 *
 * @see ProcessoIniciadoEvento
 */
@Getter
public class ProcessoCriadoEvento extends ApplicationEvent {
    private final Long idProcesso;

    public ProcessoCriadoEvento(Object fonte, Long codProcesso) {
        super(fonte);
        this.idProcesso = codProcesso;
    }

}