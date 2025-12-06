package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
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

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Autowired(required = false)
    private UnidadeRepo unidadeRepo;

    @Autowired(required = false)
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
                System.err.println(e.getMessage());
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
                unidade
        );
        final Unidade finalUnidade = unidade;
        Arrays.stream(customUser.perfis()).forEach(p -> {
             principal.getAtribuicoes().add(sgc.sgrh.model.UsuarioPerfil.builder().usuario(principal).unidade(finalUnidade).perfil(Perfil.valueOf(p)).build());
        });

        if (dbAvailable && usuarioRepo != null) {
            try { usuarioRepo.save(principal); } catch (Exception ignored) { }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}
