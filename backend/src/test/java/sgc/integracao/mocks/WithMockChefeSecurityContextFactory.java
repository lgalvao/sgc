package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.comum.BeanUtil;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;

import java.util.Set;



public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {



    @Override

    public SecurityContext createSecurityContext(WithMockChefe annotation) {

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        long chefeId;

        try {

            chefeId = Long.parseLong(annotation.value());

        } catch (NumberFormatException e) {

            chefeId = 333333333333L; // Default value

        }



        Usuario usuario;
        try {
            UsuarioRepo usuarioRepo = BeanUtil.getBean(UsuarioRepo.class);
            usuario = usuarioRepo.findById(chefeId).orElse(null);
        } catch (Exception e) {
            usuario = null;
        }
        
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setTituloEleitoral(chefeId);
            usuario.setNome("Chefe User");
            usuario.setEmail("chefe@example.com");
            usuario.setPerfis(Set.of(Perfil.CHEFE));
            usuario.setUnidade(new Unidade("Unidade Mock", "UM"));
        }



        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());

        context.setAuthentication(token);

        return context;

    }

}




