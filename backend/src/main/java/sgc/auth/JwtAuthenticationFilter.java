package sgc.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Filter que intercepta requisições para validar tokens JWT.
 * Extrai o token do header Authorization e valida sua autenticidade.
 * Se válido, configura o contexto de segurança do Spring Security.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Extrai o token do header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Se não houver header ou não começar com "Bearer ", pula o filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extrai o token (remove "Bearer " do início)
            final String jwt = authHeader.substring(7);
            final String titulo = jwtService.extractTitulo(jwt);

            // Se o token contém um usuário e não há autenticação no contexto
            if (titulo != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Valida o token
                if (jwtService.validateToken(jwt)) {
                    // Extrai os perfis do token
                    List<Map<String, String>> perfis = jwtService.extractPerfis(jwt);

                    // Converte perfis em authorities do Spring Security
                    List<SimpleGrantedAuthority> authorities = perfis.stream()
                            .map(perfil -> new SimpleGrantedAuthority("ROLE_" + perfil.get("perfil")))
                            .collect(Collectors.toList());

                    // Adiciona authority básica
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

                    // Cria o objeto de autenticação
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            titulo,
                            null,
                            authorities
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Define a autenticação no contexto de segurança
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("Usuário autenticado via JWT: {} com perfis: {}", titulo, perfis);
                } else {
                    log.warn("Token JWT inválido ou expirado para request: {}", request.getRequestURI());
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar token JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}