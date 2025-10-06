package sgc.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sgc.auth.dto.PerfilDto;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serviço para geração e validação de tokens JWT.
 * Responsável por criar tokens com claims personalizados e validar sua autenticidade.
 */
@Service
@Slf4j
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * Gera um token JWT para o usuário autenticado.
     *
     * @param titulo Título (CPF) do usuário
     * @param perfis Lista de perfis do usuário
     * @return Token JWT assinado
     */
    public String generateToken(String titulo, List<PerfilDto> perfis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("perfis", perfis);

        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        log.debug("Gerando token JWT para usuário: {} com expiração: {}", titulo, expirationDate);

        return Jwts.builder()
                .claims(claims)
                .subject(titulo)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrai todos os claims do token.
     *
     * @param token Token JWT
     * @return Claims contidos no token
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Extrai o título (username) do token.
     *
     * @param token Token JWT
     * @return Título do usuário
     */
    public String extractTitulo(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Extrai os perfis do token.
     *
     * @param token Token JWT
     * @return Lista de perfis do usuário
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, String>> extractPerfis(String token) {
        Claims claims = extractClaims(token);
        return (List<Map<String, String>>) claims.get("perfis");
    }

    /**
     * Valida se o token é válido (assinatura e expiração).
     *
     * @param token Token JWT a ser validado
     * @return true se o token é válido, false caso contrário
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            boolean isExpired = expiration.before(new Date());

            if (isExpired) {
                log.debug("Token expirado para usuário: {}", claims.getSubject());
            }

            return !isExpired;
        } catch (Exception e) {
            log.error("Erro ao validar token: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se o token está expirado.
     *
     * @param token Token JWT
     * @return true se expirado, false caso contrário
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaims(token).getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * Obtém a chave de assinatura baseada no secret configurado.
     *
     * @return Chave para assinar/validar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}