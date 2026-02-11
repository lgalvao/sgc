package sgc.seguranca.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.organizacao.UsuarioFacade;
import sgc.organizacao.model.Usuario;

import java.io.IOException;
import java.util.Set;

/**
 * Filtro de segurança que valida tokens JWT em cada requisição.
 * Extrai o token do header Authorization e configura o contexto de segurança.
 *
 * <p><b>Refatoração v3.0:</b> Removido @Lazy - filtros são instanciados após
 * o contexto de aplicação estar completo, não há necessidade de lazy loading.</p>
 */
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
                    usuario.setPerfilAtivo(claims.perfil());
                    usuario.setUnidadeAtivaCodigo(claims.unidadeCodigo());

                    // Carrega apenas o perfil selecionado no momento do login (par único perfil-unidade).
                    // Isso impede que permissões de outros perfis/unidades do usuário no banco interfiram na sessão atual.
                    var authorities = Set.of(claims.perfil().toGrantedAuthority());
                    usuario.setAuthorities(authorities);

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
