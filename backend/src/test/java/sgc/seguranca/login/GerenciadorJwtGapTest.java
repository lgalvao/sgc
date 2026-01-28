package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import sgc.organizacao.model.Perfil;

import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Tag("unit")
@DisplayName("GerenciadorJwt - Gap Tests for Null Claims")
class GerenciadorJwtGapTest {

    private static final String SECRET = "secure-secret-key-minimum-32-chars-length-xyz-123";

    @Test
    @DisplayName("Deve retornar empty se título for nulo (sub)")
    void deveRetornarEmptySeTituloNulo() {
        GerenciadorJwt service = criarService();
        String token = gerarTokenCustomizado(null, Perfil.ADMIN.name(), 1L);
        assertThat(service.validarToken(token)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty se perfil for nulo")
    void deveRetornarEmptySePerfilNulo() {
        GerenciadorJwt service = criarService();
        String token = gerarTokenCustomizado("123", null, 1L);
        assertThat(service.validarToken(token)).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty se unidade for nula")
    void deveRetornarEmptySeUnidadeNula() {
        GerenciadorJwt service = criarService();
        String token = gerarTokenCustomizado("123", Perfil.ADMIN.name(), null);
        assertThat(service.validarToken(token)).isEmpty();
    }

    private GerenciadorJwt criarService() {
        sgc.seguranca.config.JwtProperties props = mock(sgc.seguranca.config.JwtProperties.class);
        when(props.secret()).thenReturn(SECRET);
        return new GerenciadorJwt(props, mock(org.springframework.core.env.Environment.class));
    }

    private String gerarTokenCustomizado(String sub, String perfil, Long unidade) {
        var builder = Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)));
        
        if (sub != null) builder.subject(sub);
        if (perfil != null) builder.claim("perfil", perfil);
        if (unidade != null) builder.claim("unidade", unidade);
        
        return builder.compact();
    }
}
