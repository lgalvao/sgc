package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;

import java.util.Set;

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
        long gestorId;
        try {
            gestorId = Long.parseLong(customUser.value());
        } catch (NumberFormatException e) {
            gestorId = 222222222222L; // Default value
        }

        final Long finalGestorId = gestorId;
        Usuario principal = usuarioRepo.findById(finalGestorId)
            .orElseGet(() -> {
                Usuario gestor = new Usuario();
                gestor.setTituloEleitoral(finalGestorId);
                gestor.setNome("Gestor User");
                gestor.setEmail("gestor@example.com");
                gestor.setPerfis(Set.of(Perfil.GESTOR));
                return usuarioRepo.save(gestor);
            });

        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}