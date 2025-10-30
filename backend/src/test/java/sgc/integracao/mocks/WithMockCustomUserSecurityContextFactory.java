package sgc.integracao.mocks;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import sgc.comum.BeanUtil;
import sgc.sgrh.modelo.Perfil;
import sgc.sgrh.modelo.Usuario;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UnidadeRepo unidadeRepo = BeanUtil.getBean(UnidadeRepo.class);

        // A responsabilidade de criar a Unidade agora é do próprio teste.
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
