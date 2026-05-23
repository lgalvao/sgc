package sgc.seguranca.login;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.web.filter.*;
import sgc.comum.util.*;
import sgc.organizacao.*;
import sgc.organizacao.model.*;

import java.io.*;
import java.util.*;

/**
 * Filtro de segurança que valida tokens JWT em cada requisição.
 * Extrai o token do header Authorization e configura o contexto de segurança.
 */
@RequiredArgsConstructor
@Slf4j
public class FiltroJwt extends OncePerRequestFilter {
    private final GerenciadorJwt gerenciadorJwt;
    private final UsuarioFacade usuarioService;
    private final ListaNegraJwt listaNegraJwt;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return "/api/usuarios/login".equals(uri)
                || "/api/usuarios/entrar".equals(uri)
                || "/api/usuarios/logout".equals(uri)
                || uri.startsWith("/e2e/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String jwtToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken == null) {
            // Suporte a Bearer token para integrações com clientes não-browser (ex: API clients, testes E2E).
            // Em produção, o uso padrão é via cookie HttpOnly.
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
            }
        }

        if (jwtToken != null) {

            gerenciadorJwt.validarToken(jwtToken).ifPresent(claims -> {
                if (listaNegraJwt.estaRevogado(claims.jti())) {
                    log.debug("JWT revogado ignorado: {}", claims.jti());
                    return;
                }

                Usuario usuario = usuarioService.carregarUsuarioSemAtribuicoesParaAutenticacao(claims.tituloEleitoral());

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
                    log.warn("Usuário {} do JWT não encontrado no SGRH", MascaraUtil.mascarar(claims.tituloEleitoral()));
                }
            });
        }

        filterChain.doFilter(request, response);
    }
}
