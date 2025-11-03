package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.unidade.modelo.Unidade;

import java.util.Set;



public class WithMockGestorSecurityContextFactory implements WithSecurityContextFactory<WithMockGestor> {



    @Override

    public SecurityContext createSecurityContext(WithMockGestor customUser) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        long gestorId;

        try {

            gestorId = Long.parseLong(customUser.value());

        } catch (NumberFormatException e) {

            gestorId = 222222222222L; // Default value

        }



        Usuario principal = new Usuario();

        principal.setTituloEleitoral(gestorId);

        principal.setNome("Gestor User");

        principal.setEmail("gestor@example.com");

        principal.setPerfis(Set.of(Perfil.GESTOR));

        principal.setUnidade(new Unidade("Unidade Mock", "UO_SUP"));



        Authentication auth =

                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;

    }

}
