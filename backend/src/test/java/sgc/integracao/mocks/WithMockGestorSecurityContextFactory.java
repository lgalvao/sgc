package sgc.integracao.mocks;

import org.jspecify.annotations.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.security.test.context.support.*;
import org.springframework.stereotype.*;
import sgc.organizacao.model.*;

import java.util.*;

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
