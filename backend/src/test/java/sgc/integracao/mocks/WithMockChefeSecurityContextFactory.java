package sgc.integracao.mocks;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class WithMockChefeSecurityContextFactory
        implements WithSecurityContextFactory<WithMockChefe> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Autowired(required = false)
    private UnidadeRepo unidadeRepo;

    @Autowired(required = false)
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    public @Nullable SecurityContext createSecurityContext(@NonNull WithMockChefe annotation) {
        String titulo = annotation.value();
        Usuario usuario = usuarioRepo.findById(titulo)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado no data.sql: " + titulo));

        // Carregar atribuições do banco de dados
        var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(titulo);
        if (atribuicoes.isEmpty()) {
            throw new IllegalStateException("Usuário " + titulo + " não possui perfis no data.sql");
        }

        // Define perfil ativo como CHEFE
        usuario.setPerfilAtivo(Perfil.CHEFE);

        // Busca a unidade onde ele é CHEFE
        UsuarioPerfil chefia = atribuicoes.stream()
                .filter(a -> a.getPerfil() == Perfil.CHEFE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário " + titulo + " não possui perfil de CHEFE no data.sql"));

        usuario.setUnidadeAtivaCodigo(chefia.getUnidadeCodigo());

        Set<GrantedAuthority> authorities = Set.of(Perfil.CHEFE.toGrantedAuthority());
        usuario.setAuthorities(authorities);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(usuario, null, authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        return context;
    }
}
