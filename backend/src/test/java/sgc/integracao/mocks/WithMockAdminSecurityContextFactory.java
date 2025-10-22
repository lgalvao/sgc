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
public class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {
    private final UsuarioRepo usuarioRepo;

    @Autowired
    public WithMockAdminSecurityContextFactory(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Long adminId = 111111111111L;

        Usuario principal = usuarioRepo.findById(adminId)
            .orElseGet(() -> {
                Usuario admin = new Usuario();
                admin.setTituloEleitoral(adminId);
                admin.setNome("Admin User");
                admin.setEmail("admin@example.com");
                admin.setPerfis(Set.of(Perfil.ADMIN));
                return usuarioRepo.save(admin);
            });


        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}