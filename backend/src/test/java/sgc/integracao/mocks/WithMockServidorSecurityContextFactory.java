package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

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
