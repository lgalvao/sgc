package sgc.comum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class FiltroAutenticacaoSimulado extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // O token é apenas um Base64 de um JSON simulado
                String json = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
                JsonNode node = objectMapper.readTree(json);

                if (node.has("tituloEleitoral")) {
                    String titulo = node.get("tituloEleitoral").asString();
                    String perfil = node.has("perfil") ? node.get("perfil").asString() : "USER";

                    // Cria autenticação simples
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                titulo,
                                null, 
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + perfil))
                                );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                // Token inválido, ignora e deixa o SecurityContext vazio (vai dar 401 depois)
                logger.warn("Falha ao validar token simulado", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
