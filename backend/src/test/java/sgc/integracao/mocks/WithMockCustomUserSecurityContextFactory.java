package sgc.integracao.mocks;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class WithMockCustomUserSecurityContextFactory
        implements WithSecurityContextFactory<WithMockCustomUser> {
    @Autowired(required = false)
    private UnidadeRepo unidadeRepo;

    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Override
    public @Nullable SecurityContext createSecurityContext(@NonNull WithMockCustomUser customUser) {
        String titulo = customUser.tituloEleitoral();
        Usuario principal = usuarioRepo.findById(titulo)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado no data.sql: " + titulo));

        Unidade unidade = unidadeRepo.findById(customUser.unidadeId())
                .orElseThrow(() -> new IllegalStateException("Unidade " + customUser.unidadeId() + " não encontrada no data.sql"));

        // Define perfil ativo (pega o primeiro dos informados na anotação)
        if (customUser.perfis().length > 0) {
            principal.setPerfilAtivo(Perfil.valueOf(customUser.perfis()[0]));
        } else {
            principal.setPerfilAtivo(Perfil.SERVIDOR);
        }
        
        principal.setUnidadeAtivaCodigo(unidade.getCodigo());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}