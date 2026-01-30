package sgc.integracao.mocks;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import sgc.organizacao.model.Perfil;
import sgc.organizacao.model.Unidade;
import sgc.organizacao.model.Usuario;
import sgc.organizacao.model.UsuarioRepo;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import sgc.organizacao.model.UsuarioPerfil;
import sgc.organizacao.model.UsuarioPerfilRepo;

@Component
public class WithMockGestorSecurityContextFactory
        implements WithSecurityContextFactory<WithMockGestor> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Autowired(required = false)
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    public SecurityContext createSecurityContext(WithMockGestor customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        String tituloGestor = customUser.value();
        Usuario principal = null;
        if (usuarioRepo != null) {
            try {
                principal = usuarioRepo.findById(tituloGestor).orElse(null);
                // Carregar atribuições do banco de dados se o usuário existir
                if (principal != null && usuarioPerfilRepo != null) {
                    var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(tituloGestor);
                    Set<GrantedAuthority> simpleAuthorities = atribuicoes.stream()
                            .map(a -> a.getPerfil().toGrantedAuthority())
                            .collect(Collectors.toSet());
                    principal.setAuthorities(simpleAuthorities);
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        if (principal == null) {
            principal = new Usuario();
            principal.setTituloEleitoral(tituloGestor);
            principal.setNome("Gestor User");
            principal.setEmail("gestor@example.com");
            Unidade u = Unidade.builder().nome("Unidade Mock").sigla("UO_SUP").build();
            principal.setUnidadeLotacao(u);

            Set<UsuarioPerfil> atribuicoes = new HashSet<>();
            atribuicoes.add(
                    UsuarioPerfil.builder()
                            .usuario(principal)
                            .unidade(u)
                            .perfil(Perfil.GESTOR)
                            .build());

            Set<GrantedAuthority> simpleAuthorities = atribuicoes.stream()
                    .map(a -> a.getPerfil().toGrantedAuthority())
                    .collect(Collectors.toSet());
            principal.setAuthorities(simpleAuthorities);
        } else {
            Set<UsuarioPerfil> atribuicoes = new HashSet<>(principal.getTodasAtribuicoes(new HashSet<>()));
            if (atribuicoes.stream()
                    .noneMatch(a -> a.getPerfil() == Perfil.GESTOR)) {
                // Usuário existe mas não tem perfil GESTOR, adicionar com sua unidade de lotação
                Unidade unidade = principal.getUnidadeLotacao();
                atribuicoes.add(
                        UsuarioPerfil.builder()
                                .usuario(principal)
                                .unidade(unidade)
                                .perfil(Perfil.GESTOR)
                                .build());
            }
            Set<GrantedAuthority> simpleAuthorities = atribuicoes.stream()
                    .map(a -> a.getPerfil().toGrantedAuthority())
                    .collect(Collectors.toSet());
            principal.setAuthorities(simpleAuthorities);
        }

        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        principal, "password", principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}
