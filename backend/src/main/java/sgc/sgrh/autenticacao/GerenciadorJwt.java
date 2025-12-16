package sgc.sgrh.autenticacao;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sgc.sgrh.model.Perfil;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class GerenciadorJwt {
    private final JwtProperties jwtProperties;
    
    private SecretKey getSigningKey() {
        // Garante que a chave tenha tamanho adequado (mínimo 256 bits para HS256)
        String secret = jwtProperties.getSecret();
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret deve ter no mínimo 32 caracteres");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.getExpiracaoMinutos(), ChronoUnit.MINUTES);

        String token = Jwts.builder()
                .subject(tituloEleitoral)
                .claim("perfil", perfil.name())
                .claim("unidade", unidadeCodigo)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();

        log.info("JWT gerado para usuário {} com perfil {} na unidade {}", 
            tituloEleitoral, perfil, unidadeCodigo);
        
        return token;
    }

    public Optional<JwtClaims> validarToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tituloEleitoral = claims.getSubject();
            String perfilStr = claims.get("perfil", String.class);
            Long unidadeCodigo = claims.get("unidade", Long.class);

            if (tituloEleitoral == null || perfilStr == null || unidadeCodigo == null) {
                log.warn("JWT com claims inválidos");
                return Optional.empty();
            }

            Perfil perfil = Perfil.valueOf(perfilStr);
            
            log.debug("JWT válido para usuário {} com perfil {} na unidade {}", 
                tituloEleitoral, perfil, unidadeCodigo);

            return Optional.of(new JwtClaims(tituloEleitoral, perfil, unidadeCodigo));
            
        } catch (Exception e) {
            log.debug("JWT inválido ou expirado: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record JwtClaims(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {}
}
