package sgc.integracao.mocks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.UnidadeRepo;

import java.util.Arrays;

@Slf4j
@Component
public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
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
                log.error("Erro ao buscar unidade", e);
            }
        }

        if (unidade == null) {
            unidade = new Unidade("Unidade Mock", "MOCK");
            unidade.setCodigo(customUser.unidadeId());
        }

        Usuario principal = Usuario.builder()
                .tituloEleitoral(customUser.tituloEleitoral())
                .nome(customUser.nome())
                .email(customUser.email())
                .ramal("321")
                .unidadeLotacao(unidade)
                .build();
        final Unidade finalUnidade = unidade;
        Arrays.stream(customUser.perfis())
                .forEach(
                        p -> principal
                                .getAtribuicoes()
                                .add(
                                        sgc.organizacao.model.UsuarioPerfil.builder()
                                                .usuario(principal)
                                                .unidade(finalUnidade)
                                                .perfil(Perfil.valueOf(p))
                                                .build()));

        if (dbAvailable && usuarioRepo != null) {
            try {
                usuarioRepo.save(principal);
            } catch (Exception e) {
                log.error("Erro ao salvar usuario", e);
            }
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        context.setAuthentication(authentication);

        return context;
    }
}