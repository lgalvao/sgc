package sgc.integracao.mocks;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import sgc.organizacao.model.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WithMockGestorSecurityContextFactory
        implements WithSecurityContextFactory<WithMockGestor> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Autowired(required = false)
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    public @Nullable SecurityContext createSecurityContext(@NonNull WithMockGestor customUser) {
        String titulo = customUser.value();
        Usuario usuario = usuarioRepo.findById(titulo)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado no data.sql: " + titulo));

        var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(titulo);
        if (atribuicoes.isEmpty()) {
            throw new IllegalStateException("Usuário " + titulo + " não possui perfis no data.sql");
        }

        usuario.setPerfilAtivo(Perfil.GESTOR);

        UsuarioPerfil gestao = atribuicoes.stream()
                .filter(a -> a.getPerfil() == Perfil.GESTOR)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário " + titulo + " não possui perfil de GESTOR no data.sql"));

        usuario.setUnidadeAtivaCodigo(gestao.getUnidadeCodigo());

        Set<GrantedAuthority> authorities = Set.of(Perfil.GESTOR.toGrantedAuthority());
        usuario.setAuthorities(authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(usuario, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
