package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.comum.modelo.AdministradorRepo;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;

public class WithMockChefeSecurityContextFactory implements WithSecurityContextFactory<WithMockChefe> {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Autowired
    private AdministradorRepo administradorRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockChefe annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Usuario usuario = usuarioRepo.findByTitulo(annotation.value())
            .orElseGet(() -> {
                Usuario newUser = new Usuario();
                newUser.setTitulo(annotation.value());
                return usuarioRepo.save(newUser);
            });

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.determineAuthorities(administradorRepo));
        context.setAuthentication(token);
        return context;
    }
}