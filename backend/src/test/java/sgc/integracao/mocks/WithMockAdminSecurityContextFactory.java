package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.comum.modelo.AdministradorRepo;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;

@Component
public class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {

    private final UsuarioRepo usuarioRepo;
    private final AdministradorRepo administradorRepo;

    @Autowired
    public WithMockAdminSecurityContextFactory(UsuarioRepo usuarioRepo, AdministradorRepo administradorRepo) {
        this.usuarioRepo = usuarioRepo;
        this.administradorRepo = administradorRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Usuario principal = usuarioRepo.findById("admin")
            .orElseGet(() -> usuarioRepo.save(new Usuario("admin", "Admin User", "admin@example.com", null, null, null)));


        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.determineAuthorities(administradorRepo));
        context.setAuthentication(auth);
        return context;
    }
}