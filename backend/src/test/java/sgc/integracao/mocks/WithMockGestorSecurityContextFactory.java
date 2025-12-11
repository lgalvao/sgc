package sgc.integracao.mocks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.sgrh.model.Perfil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import sgc.unidade.model.Unidade;

import java.util.HashSet;
import java.util.Set;

@Component
public class WithMockGestorSecurityContextFactory
        implements WithSecurityContextFactory<WithMockGestor> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockGestor customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String tituloGestor = customUser.value();
        Usuario principal = null;
        if (usuarioRepo != null) {
            try {
                principal = usuarioRepo.findById(tituloGestor).orElse(null);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(tituloGestor);
            principal.setNome("Gestor User");
            principal.setEmail("gestor@example.com");
            Unidade u = new Unidade("Unidade Mock", "UO_SUP");
            principal.setUnidadeLotacao(u);

            Set<sgc.sgrh.model.UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(
                            sgc.sgrh.model.UsuarioPerfil.builder()
                                    .usuario(principal)
                                    .unidade(u)
                                    .perfil(Perfil.GESTOR)
                                    .build());
            principal.setAtribuicoes(atribuicoes);

        } else {
            Set<sgc.sgrh.model.UsuarioPerfil> atribuicoes = new HashSet<>(principal.getAtribuicoes());
            if (atribuicoes.stream()
                    .noneMatch(a -> a.getPerfil() == Perfil.GESTOR)) {
                Unidade u = new Unidade("Unidade Mock", "UO_SUP");
                atribuicoes.add(
                                sgc.sgrh.model.UsuarioPerfil.builder()
                                        .usuario(principal)
                                        .unidade(u)
                                        .perfil(Perfil.GESTOR)
                                        .build());
                principal.setAtribuicoes(atribuicoes);
            }
        }

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
