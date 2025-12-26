package sgc.usuario.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum Perfil {
    ADMIN,
    GESTOR,
    CHEFE,
    SERVIDOR;

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority("ROLE_%s".formatted(this.name()));
    }
}
