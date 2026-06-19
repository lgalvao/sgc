package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;

import java.util.HashSet;
import java.util.Set;

@Component
public class WithMockServidorSecurityContextFactory
        implements WithSecurityContextFactory<WithMockServidor> {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockServidor customUser) {
        String titulo = customUser.tituloEleitoral();
        Usuario principal = usuarioRepo.findById(titulo).orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setTituloEleitoral(titulo);
            novo.setNome(customUser.nome());
            novo.setEmail("servidor@teste.com");
            return usuarioRepo.saveAndFlush(novo);
        });

        principal.setPerfilAtivo(Perfil.SERVIDOR);
        
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(Perfil.SERVIDOR.toGrantedAuthority());
        principal.setAuthorities(authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
