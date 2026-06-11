package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

@Component
public class WithMockUsuarioSecurityContextFactory
        implements WithSecurityContextFactory<WithMockUsuario> {

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockUsuario customUser) {
        String titulo = customUser.tituloEleitoral();
        Usuario principal = usuarioRepo.findById(titulo).orElseGet(() -> {
            Usuario novo = new Usuario();
            novo.setTituloEleitoral(titulo);
            novo.setNome(customUser.nome());
            novo.setEmail("usuario@teste.com");
            return usuarioRepo.saveAndFlush(novo);
        });

        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USUARIO"));
        principal.setAuthorities(authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
