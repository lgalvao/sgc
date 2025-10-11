package sgc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;

public class WithMockGestorSecurityContextFactory implements WithSecurityContextFactory<WithMockGestor> {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockGestor annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Usuario usuario = usuarioRepo.findByTitulo(annotation.value())
            .orElseGet(() -> {
                Usuario newUser = new Usuario();
                newUser.setTitulo(annotation.value());
                return usuarioRepo.save(newUser);
            });

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
        context.setAuthentication(token);
        return context;
    }
}