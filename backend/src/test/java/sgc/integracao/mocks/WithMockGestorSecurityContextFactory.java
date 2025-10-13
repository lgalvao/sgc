package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;

@Component
public class WithMockGestorSecurityContextFactory implements WithSecurityContextFactory<WithMockGestor> {

    private final UsuarioRepo usuarioRepo;

    @Autowired
    public WithMockGestorSecurityContextFactory(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockGestor customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Usuario principal = usuarioRepo.findById(customUser.value())
            .orElseGet(() -> usuarioRepo.save(new Usuario(customUser.value(), "Gestor User", "gestor@example.com", null, null, null)));

        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}