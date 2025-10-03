package sgc.auth.events;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;
import sgc.dto.PerfilUnidadeDTO;

import java.time.Instant;
import java.util.List;

/**
 * Evento publicado quando um login é autenticado com sucesso.
 * Contém título do usuário e perfis/unidades retornados pelo Sistema Acesso.
 */
@Getter
@Setter
public class LoginSuccessEvent extends ApplicationEvent {
    private final String titulo;
    private final List<PerfilUnidadeDTO> perfis;
    private final Instant when;

    public LoginSuccessEvent(Object source, String titulo, List<PerfilUnidadeDTO> perfis) {
        super(source);
        this.titulo = titulo;
        this.perfis = perfis;
        this.when = Instant.now();
    }
}