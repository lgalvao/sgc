package sgc.integracao.mocks;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
public class WithMockAdminSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAdmin> {
    @Autowired(required = false)
    private UsuarioRepo usuarioRepo;

    @Autowired(required = false)
    private UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    public @Nullable SecurityContext createSecurityContext(@NonNull WithMockAdmin customUser) {
        String tituloAdmin = "111111111111"; // Título padrão Admin no data.sql
        Usuario principal = usuarioRepo.findById(tituloAdmin)
                .orElseThrow(() -> new IllegalStateException("Usuário Admin (111111111111) não encontrado no data.sql"));

        var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(tituloAdmin);
        if (atribuicoes.isEmpty()) {
            throw new IllegalStateException("Usuário Admin não possui perfis no data.sql");
        }

        principal.setPerfilAtivo(Perfil.ADMIN);
        
        // ADMIN geralmente atua em unidades centrais (ex: 100 no data.sql)
        UsuarioPerfil adminPerfil = atribuicoes.stream()
                .filter(a -> a.getPerfil() == Perfil.ADMIN)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Usuário 111111111111 não possui perfil de ADMIN no data.sql"));

        principal.setUnidadeAtivaCodigo(adminPerfil.getUnidadeCodigo());

        Set<GrantedAuthority> authorities = Set.of(Perfil.ADMIN.toGrantedAuthority());
        principal.setAuthorities(authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
