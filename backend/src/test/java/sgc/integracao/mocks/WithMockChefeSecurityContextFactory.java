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
public class WithMockChefeSecurityContextFactory
        implements WithSecurityContextFactory<WithMockChefe> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

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
