package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;

import java.util.Set;

@Component
public class WithMockGestorSecurityContextFactory implements WithSecurityContextFactory<WithMockGestor> {

    private final UsuarioRepo usuarioRepo;

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
            gestorId = 222222222222L;
        }

        Usuario principal;
        try {
            principal = usuarioRepo.findById(gestorId).orElse(null);
        } catch (Exception e) {
            principal = null;
        }
        
        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(gestorId);
            principal.setNome("Gestor User");
            principal.setEmail("gestor@example.com");
            principal.setPerfis(Set.of(Perfil.GESTOR));
            principal.setUnidade(new Unidade("Unidade Mock", "UO_SUP"));
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;
    }
}




