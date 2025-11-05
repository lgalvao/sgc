package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.comum.BeanUtil;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;

import java.util.Set;



public class WithMockAdminSecurityContextFactory implements WithSecurityContextFactory<WithMockAdmin> {



    @Override

    public SecurityContext createSecurityContext(WithMockAdmin customUser) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Long adminId = 111111111111L;



        Usuario principal;
        try {
            UsuarioRepo usuarioRepo = BeanUtil.getBean(UsuarioRepo.class);
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



        Authentication auth =

                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());

        context.setAuthentication(auth);

        return context;

    }

}




