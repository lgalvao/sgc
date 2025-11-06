package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.unidade.model.Unidade;
import sgc.unidade.model.UnidadeRepo;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    private final UnidadeRepo unidadeRepo;

    public WithMockCustomUserSecurityContextFactory(UnidadeRepo unidadeRepo) {
        this.unidadeRepo = unidadeRepo;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Unidade unidade = unidadeRepo.findById(customUser.unidadeId())
                .orElseThrow(() -> new IllegalStateException(
                        "A Unidade de teste com código %d não foi encontrada. Garanta quefoi criada no @BeforeEach do teste."
                        .formatted(customUser.unidadeId()))
                );

        Usuario principal = new Usuario(
                customUser.tituloEleitoral(),
                customUser.nome(),
                customUser.email(),
                "321",
                unidade,
                Arrays.stream(customUser.perfis()).map(Perfil::valueOf).collect(Collectors.toList())
        );

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}
