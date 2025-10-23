package sgc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sgc.sgrh.Perfil;
import sgc.sgrh.Usuario;
import sgc.sgrh.UsuarioRepo;
import sgc.sgrh.UsuarioService;
import sgc.unidade.modelo.Unidade;
import sgc.unidade.modelo.UnidadeRepo;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
@Profile("e2e")
@RequiredArgsConstructor
public class JwtMockFilter extends OncePerRequestFilter {

    private final UsuarioRepo usuarioRepo;
    private final UnidadeRepo unidadeRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7); // Remover "Bearer "

            try {
                // Decodificar o token simulado (Base64)
                String decodedClaims = new String(Base64.getDecoder().decode(token));
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> claims = objectMapper.readValue(decodedClaims, HashMap.class);

                Long tituloEleitoral = Long.valueOf(claims.get("tituloEleitoral").toString());
                String perfilString = claims.get("perfil").toString();
                Long unidadeCodigo = Long.valueOf(claims.get("unidadeCodigo").toString());

                // Buscar usuário e unidade no repositório (simulando UserDetailsService)
                Usuario usuario = usuarioRepo.findByTituloEleitoral(tituloEleitoral)
                        .orElseThrow(() -> new RuntimeException("Usuário não encontrado no token simulado."));

                Unidade unidade = unidadeRepo.findById(unidadeCodigo)
                        .orElseThrow(() -> new RuntimeException("Unidade não encontrada no token simulado."));

                // Criar um objeto Usuario com o perfil e unidade do token
                // Nota: Esta é uma simplificação. Em um JWT real, o perfil e a unidade
                // seriam extraídos dos claims e usados para construir as authorities.
                Usuario authenticatedUser = new Usuario(
                        usuario.getTituloEleitoral(),
                        usuario.getNome(),
                        usuario.getEmail(),
                        usuario.getRamal(),
                        unidade, // Usar a unidade do token
                        Set.of(Perfil.valueOf(perfilString)) // Usar o perfil do token
                );


                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        authenticatedUser, null, authenticatedUser.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                System.err.println("Erro ao processar token simulado: " + e.getMessage());
                // Em um cenário real, você pode querer retornar um 401 aqui
            }
        }

        filterChain.doFilter(request, response);
    }
}
