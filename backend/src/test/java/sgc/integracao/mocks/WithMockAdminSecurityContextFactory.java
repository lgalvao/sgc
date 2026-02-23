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

import java.util.Set;

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
        
        // ADMIN atua na unidade raiz (id=1) conforme acesso.md
        principal.setUnidadeAtivaCodigo(1L);

        Set<GrantedAuthority> authorities = Set.of(Perfil.ADMIN.toGrantedAuthority());
        principal.setAuthorities(authorities);

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, "password", authorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
