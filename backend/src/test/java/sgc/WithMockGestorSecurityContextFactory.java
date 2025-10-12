package sgc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sgc.comum.modelo.Usuario;
import sgc.comum.modelo.UsuarioRepo;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

@Component
public class WithMockGestorSecurityContextFactory implements WithSecurityContextFactory<WithMockGestor> {

    @Autowired
    private UnidadeRepo unidadeRepo;

    @Autowired
    private UsuarioRepo usuarioRepo;

    @Override
    @Transactional
    public SecurityContext createSecurityContext(WithMockGestor customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Garante que a unidade de teste exista.
        Unidade unidade = unidadeRepo.findBySigla("UT")
            .orElseGet(() -> unidadeRepo.save(new Unidade("Unidade de Teste", "UT")));

        Usuario principal = new Usuario();
        principal.setTitulo(customUser.value());
        principal.setNome("Gestor da " + unidade.getSigla());
        principal.setUnidade(unidade);
        unidade.setTitular(principal);
        usuarioRepo.save(principal);
        unidadeRepo.save(unidade);


        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}