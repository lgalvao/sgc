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
import sgc.unidade.model.UnidadeRepo;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UnidadeRepo unidadeRepo;
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Unidade unidade = null;
        boolean dbAvailable = false;
        if (unidadeRepo != null) {
            try {
                unidade = unidadeRepo.findById(customUser.unidadeId()).orElse(null);
                dbAvailable = true;
            } catch (Exception e) {
                unidade = null;
            }
        }

        if (unidade == null) {
             unidade = new Unidade("Unidade Mock", "MOCK");
             unidade.setCodigo(customUser.unidadeId());
        }

        Usuario principal = new Usuario(
                customUser.tituloEleitoral(),
                customUser.nome(),
                customUser.email(),
                "321",
                unidade,
                Arrays.stream(customUser.perfis()).map(Perfil::valueOf).collect(Collectors.toList())
        );

        if (dbAvailable && usuarioRepo != null) {
            try { usuarioRepo.save(principal); } catch (Exception e) { }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}
