package sgc.comum.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.comum.util.TokenSimuladoUtil;
import sgc.sgrh.model.Usuario;
import sgc.sgrh.model.UsuarioRepo;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FiltroAutenticacaoSimulado extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private final UsuarioRepo usuarioRepo;

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
                String[] parts = token.split("\\.");
                if (parts.length != 2 || !TokenSimuladoUtil.validar(parts[0], parts[1])) {
                    throw new IllegalArgumentException("Token inválido ou assinatura incorreta");
                }

                // O token é apenas um Base64 de um JSON simulado
                String json = new String(Base64.getDecoder().decode(parts[0]), StandardCharsets.UTF_8);
                JsonNode node = objectMapper.readTree(json);

                if (node.has("tituloEleitoral")) {
                    String titulo = node.get("tituloEleitoral").asString();
                    String perfil = node.has("perfil") ? node.get("perfil").asString() : "USER";

                    // Load Usuario entity from database to enable @AuthenticationPrincipal Usuario
                    Optional<Usuario> usuarioOpt = usuarioRepo.findById(titulo);
                    Object principal = usuarioOpt.isPresent() ? usuarioOpt.get() : titulo;

                    // Cria autenticação com Usuario entity como principal
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    principal,
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

