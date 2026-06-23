package sgc.organizacao.model;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

public enum Perfil {
    ADMIN,
    GESTOR,
    CHEFE,
    SERVIDOR;

    public GrantedAuthority toGrantedAuthority() {
        return new SimpleGrantedAuthority("ROLE_%s".formatted(this.name()));
    }
}
