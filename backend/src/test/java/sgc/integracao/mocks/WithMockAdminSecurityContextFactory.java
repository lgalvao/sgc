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

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String tituloAdmin = "111111111111";

        Usuario principal = null;
        boolean dbAvailable = false;
        if (usuarioRepo != null) {
            try {
                principal = usuarioRepo.findById(tituloAdmin).orElse(null);
                dbAvailable = true;
            } catch (Exception e) {
                principal = null;
            }
        }
        
        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(tituloAdmin);
            principal.setNome("Admin User");
            principal.setEmail("admin@example.com");
            principal.setPerfis(Set.of(Perfil.ADMIN));
            principal.setUnidade(new Unidade("Unidade Mock", "UM"));
            if (dbAvailable) {
                try { usuarioRepo.save(principal); } catch (Exception e) { }
            }
        } else {
            principal.setPerfis(Set.of(Perfil.ADMIN));
            if (dbAvailable) {
                try { usuarioRepo.save(principal); } catch (Exception e) { }
            }
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;
    }
}




