package sgc.seguranca.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;

import java.io.IOException;

/**
 * Filtro de segurança que valida tokens JWT em cada requisição.
 * Extrai o token do header Authorization e configura o contexto de segurança.
 *
 * <p><b>Refatoração v3.0:</b> Removido @Lazy - filtros são instanciados após
 * o contexto de aplicação estar completo, não há necessidade de lazy loading.</p>
 */
@Component
@Profile({"!test", "secure-test"})
@RequiredArgsConstructor
@Slf4j
public class FiltroJwt extends OncePerRequestFilter {
    private final GerenciadorJwt gerenciadorJwt;
    private final UsuarioFacade usuarioService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);

            gerenciadorJwt.validarToken(jwtToken).ifPresent(claims -> {
                Usuario usuario = usuarioService.carregarUsuarioParaAutenticacao(claims.tituloEleitoral());

                if (usuario != null) {
                    var authorities = usuario.getAuthorities();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            usuario,
                            null,
                            authorities);

                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("Usuário {} do JWT não encontrado no SGRH", claims.tituloEleitoral());
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}
