package sgc.comum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.sgrh.autenticacao.GerenciadorJwt;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FiltroAutenticacaoSimulado extends OncePerRequestFilter {
    private final GerenciadorJwt gerenciadorJwt;
    @Lazy
    private final UsuarioRepo usuarioRepo;
    @Lazy
    private final sgc.sgrh.model.UsuarioPerfilRepo usuarioPerfilRepo;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            
            gerenciadorJwt.validarToken(jwtToken).ifPresent(claims -> {
                Optional<Usuario> usuarioOpt = usuarioRepo.findById(claims.tituloEleitoral());
                
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    
                    // Carregar atribuições do banco
                    var atribuicoes = usuarioPerfilRepo.findByUsuarioTitulo(usuario.getTituloEleitoral());
                    usuario.setAtribuicoes(new java.util.HashSet<>(atribuicoes));
                    
                    log.debug("Carregando authorities para usuário {}", claims.tituloEleitoral());
                    var authorities = usuario.getAuthorities();
                    log.debug("Authorities carregadas: {}", authorities);
                    
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            authorities
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    log.debug("Usuário {} autenticado via JWT com authorities: {}", 
                        claims.tituloEleitoral(), authorities);
                } else {
                    log.warn("Usuário {} do JWT não encontrado no SGRH", claims.tituloEleitoral());
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}


