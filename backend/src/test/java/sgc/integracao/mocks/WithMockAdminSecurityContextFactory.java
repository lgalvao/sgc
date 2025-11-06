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
public class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {

    private final UsuarioRepo usuarioRepo;

    public WithMockAdminSecurityContextFactory(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Long adminId = 111111111111L;

        Usuario principal;
        try {
            principal = usuarioRepo.findById(adminId).orElse(null);
        } catch (Exception e) {
            principal = null;
        }
        
        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(adminId);
            principal.setNome("Admin User");
            principal.setEmail("admin@example.com");
            principal.setPerfis(Set.of(Perfil.ADMIN));
            principal.setUnidade(new Unidade("Unidade Mock", "UM"));
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;
    }
}




