package sgc.seguranca.login;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import jakarta.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.springframework.core.env.*;
import org.springframework.stereotype.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.seguranca.config.*;

import javax.crypto.*;
import java.nio.charset.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;

/**
 * Gerenciador de tokens JWT para autenticação.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GerenciadorJwt {
    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-32-chars";
    private final JwtProperties jwtProperties;
    private final Environment environment;

    @PostConstruct
    public void verificarSegurancaChave() {
        String secret = jwtProperties.secret();
        if (secret == null || secret.length() < 32) {
            log.error("🚨 ERRO CRÍTICO DE SEGURANÇA: JWT secret deve ter no mínimo 32 caracteres. Valor não resolvido ou insuficiente.");
            throw new ErroConfiguracao("JWT secret deve ter no mínimo 32 caracteres");
        }

        if (DEFAULT_SECRET.equals(secret)) {
            // Permite uso da chave padrão em ambientes de desenvolvimento/teste
            if (environment.acceptsProfiles(Profiles.of("test", "e2e", "local", "hom", "default"))) {
                log.debug("ALERTA: Aplicação está usando o segredo JWT padrão.");
            } else {
                log.error("🚨 ERRO CRÍTICO DE SEGURANÇA: Tentativa de iniciar em ambiente de produção com o segredo JWT padrão.");
                throw new ErroConfiguracao("FALHA DE SEGURANÇA: Segredo JWT padrão não permitido em produção");
            }
        }
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String gerarToken(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
        Instant now = Instant.now();
        Instant expiration = now.plus(jwtProperties.expiracaoMinutos(), ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(tituloEleitoral)
                .claim("perfil", perfil.name())
                .claim("unidade", unidadeCodigo)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
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

            @SuppressWarnings("ConstantConditions")
            boolean incompleto = tituloEleitoral == null || perfilStr == null || unidadeCodigo == null;
            if (incompleto) {
                log.warn("JWT com claims obrigatórios ausentes");
                return Optional.empty();
            }

            Perfil perfil = Perfil.valueOf(perfilStr);
            return Optional.of(new JwtClaims(tituloEleitoral, perfil, unidadeCodigo));

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Falha na validação do JWT: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro inesperado ao validar JWT", e);
            return Optional.empty();
        }
    }

    public String gerarTokenPreAuth(String tituloEleitoral) {
        Instant now = Instant.now();
        Instant expiration = now.plus(5, ChronoUnit.MINUTES);

        return Jwts.builder()
                .subject(tituloEleitoral)
                .claim("type", "PRE_AUTH")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    public Optional<String> validarTokenPreAuth(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!"PRE_AUTH".equals(claims.get("type"))) {
                log.warn("Tentativa de uso de token inválido para pré-autenticação");
                return Optional.empty();
            }

            return Optional.ofNullable(claims.getSubject());

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Falha na validação do token pré-auth: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record JwtClaims(String tituloEleitoral, Perfil perfil, Long unidadeCodigo) {
    }
}
