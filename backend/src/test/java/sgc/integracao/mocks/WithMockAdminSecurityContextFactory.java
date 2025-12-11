package sgc.integracao.mocks;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class WithMockAdminSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAdmin> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String tituloAdmin = "111111111111";

        Usuario principal = null;
        if (usuarioRepo != null) {
            try {
                principal = usuarioRepo.findById(tituloAdmin).orElse(null);
            } catch (Exception e) {
                log.error("Erro ao buscar usuario admin", e);
            }
        }

        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(tituloAdmin);
            principal.setNome("Admin User");
            principal.setEmail("admin@example.com");
            Unidade u = new Unidade("Unidade Mock", "UM");
            principal.setUnidadeLotacao(u);

            Set<sgc.sgrh.model.UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(
                            sgc.sgrh.model.UsuarioPerfil.builder()
                                    .usuario(principal)
                                    .unidade(u)
                                    .perfil(Perfil.ADMIN)
                                    .build());
            principal.setAtribuicoes(atribuicoes);

        } else {
            Set<sgc.sgrh.model.UsuarioPerfil> atribuicoes = new HashSet<>(principal.getAtribuicoes());
            if (atribuicoes.stream().noneMatch(a -> a.getPerfil() == Perfil.ADMIN)) {
                Unidade u = new Unidade("Unidade Mock", "UM");
                atribuicoes.add(
                                sgc.sgrh.model.UsuarioPerfil.builder()
                                        .usuario(principal)
                                        .unidade(u)
                                        .perfil(Perfil.ADMIN)
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
