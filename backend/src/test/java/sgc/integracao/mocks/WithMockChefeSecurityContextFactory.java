package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

    private final UsuarioRepo usuarioRepo;

    public WithMockChefeSecurityContextFactory(UsuarioRepo usuarioRepo) {
        this.usuarioRepo = usuarioRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        String tituloChefe = annotation.value();

        Usuario usuario;
        try {
            usuario = usuarioRepo.findById(tituloChefe).orElse(null);
        } catch (Exception e) {
            usuario = null;
        }
        
        if (usuario == null) {
            usuario = new Usuario();
            usuario.setTituloEleitoral(tituloChefe);
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




