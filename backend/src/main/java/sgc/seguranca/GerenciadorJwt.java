package sgc.seguranca;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;
import sgc.usuario.model.Perfil;

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
    private final Environment environment;

    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-32-chars";

    @PostConstruct
    public void verificarSegurancaChave() {
        if (DEFAULT_SECRET.equals(jwtProperties.getSecret())) {
            if (environment.acceptsProfiles(Profiles.of("test", "e2e", "local"))) {
                log.warn("⚠️ ALERTA DE SEGURANÇA: A aplicação está rodando com o segredo JWT padrão. " +
                        "Isso é aceitável APENAS para ambientes de desenvolvimento/teste (test, e2e, local).");
            } else {
                log.error("🚨 ERRO CRÍTICO DE SEGURANÇA: Tentativa de iniciar em ambiente produtivo com o segredo JWT padrão.");
                throw new IllegalStateException(
                        "FALHA DE SEGURANÇA: A propriedade 'aplicacao.jwt.secret' não foi alterada do padrão inseguro. " +
                        "Configure a variável de ambiente JWT_SECRET com um valor seguro.");
            }
        }
    }
    
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

        log.debug("JWT gerado para usuário {} com perfil {} na unidade {}", 
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
