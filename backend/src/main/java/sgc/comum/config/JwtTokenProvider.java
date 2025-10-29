package sgc.comum.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm; // Ainda necessário para Keys.hmacShaKeyFor
import io.jsonwebtoken.io.Decoders; // Adicionar import
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {
    // Chave secreta estática para testes
    // Gerar uma chave segura e codificá-la em Base64
    // Em um cenário real, esta chave viria de uma configuração segura.
    public static final SecretKey CHAVE_SECRETA_TESTE = Keys.hmacShaKeyFor(Decoders.BASE64.decode("aW5jcmVkaXZlbG1lbnRlU2VjcmV0YUtleVBhcmFUb2tlbnNKV1RUZXN0ZXM=")); // Exemplo de chave Base64

    private final SecretKey secretKey;
    private final long validityInMilliseconds; // 1 hora

    public JwtTokenProvider() {
        this.secretKey = CHAVE_SECRETA_TESTE; // Usar a chave estática
        this.validityInMilliseconds = 3600000;
    }

    public Authentication getAuthentication(String token) {
        // Usar Jwts.parser().verifyWith() para parsear o token
        Claims claims = Jwts.parser()
                .verifyWith(secretKey) // Usar verifyWith
                .build()
                .parseSignedClaims(token) // Usar parseSignedClaims
                .getPayload(); // Usar getPayload

        String perfil = claims.get("perfil", String.class);
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(perfil));

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey) // Usar verifyWith
                    .build()
                    .parseSignedClaims(token); // Usar parseSignedClaims
            return true;
        } catch (Exception e) {
            System.err.println("Erro na validação do token: " + e.getMessage());
            return false;
        }
    }

    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
