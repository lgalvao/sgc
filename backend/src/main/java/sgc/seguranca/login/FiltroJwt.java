package sgc.seguranca.login;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.*;
import org.springframework.web.filter.*;
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
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwtToken = authHeader.substring(7);
            }
        }

        if (jwtToken != null) {

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
                    log.warn("Usuário {} do JWT não encontrado no SGRH", mascarar(claims.tituloEleitoral()));
                }
            });
        }

        filterChain.doFilter(request, response);
    }

    private String mascarar(String valor) {
        if (valor.length() <= 4) return "***";
        return "***" + valor.substring(valor.length() - 4);
    }
}
