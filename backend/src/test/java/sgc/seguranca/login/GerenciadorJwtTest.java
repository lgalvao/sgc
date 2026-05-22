package sgc.seguranca.login;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.core.env.*;
import sgc.comum.erros.*;
import sgc.organizacao.model.*;
import sgc.seguranca.config.*;

import java.nio.charset.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciadorJwt - Testes unitários")
class GerenciadorJwtTest {
    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-64-chars-for-hs512";
    private static final String DEFAULT_SECRET_SHORT = "short";
    private static final String SECURE_SECRET = "secure-secret-key-minimum-32-chars-length-xyz-123";

    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private Environment environment;
    @InjectMocks
    private GerenciadorJwt gerenciador;

    @Test
    @DisplayName("Deve inicializar com sucesso se segredo seguro em produção")
    void initSecureProd() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);
        assertThatCode(() -> gerenciador.verificarSegurancaChave())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve alertar (warn) se segredo padrão em ambiente de teste")
    void warnDefaultTest() {
        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET);
        // Correctly match the Profiles.of argument using an ArgumentMatcher
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(true);

        assertThatCode(() -> gerenciador.verificarSegurancaChave())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar se segredo padrão em produção")
    void failDefaultProd() {
        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET);
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        assertThatThrownBy(() -> gerenciador.verificarSegurancaChave())
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("FALHA DE SEGURANÇA");
    }

    @Test
    @DisplayName("Deve falhar na inicialização se segredo é muito curto ou não resolvido")
    void failInitShortSecret() {
        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET_SHORT); // A short secret or unresoved literal

        assertThatThrownBy(() -> gerenciador.verificarSegurancaChave())
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("mínimo 32 caracteres");
    }

    @Test
    @DisplayName("Deve falhar na inicialização se segredo é nulo")
    void failInitNullSecret() {
        when(jwtProperties.secret()).thenReturn(null);

        assertThatThrownBy(() -> gerenciador.verificarSegurancaChave())
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("mínimo 32 caracteres");
    }

    @Test
    @DisplayName("Deve validar token com sucesso")
    void validateSuccess() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);
        when(jwtProperties.expiracaoMinutos()).thenReturn(60);

        String token = gerenciador.gerarToken("123", Perfil.ADMIN, 1L);

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);

        assertThat(result).isPresent();
        assertThat(result.get().tituloEleitoral()).isEqualTo("123");
        assertThat(result.get().perfil()).isEqualTo(Perfil.ADMIN);
        assertThat(result.get().unidadeCodigo()).isEqualTo(1L);
        assertThat(result.get().jti()).isNotBlank();
        assertThat(result.get().expiracao()).isAfter(java.time.Instant.now().minusSeconds(1));
    }

    @Test
    @DisplayName("Deve retornar empty para token inválido")
    void validateInvalid() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);
        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken("invalid.token.here");
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty para token com claims faltando (simulado)")
    void validateMissingClaims() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        // Gerar token "mal formado" (business logic wise) mas valido (crypto wise)
        String token = Jwts.builder()
                .subject(null) // missing subject
                .signWith(Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty(); // Deve falhar no null check dos claims
    }

    @Test
    @DisplayName("Deve retornar empty para token com subject nulo mas outros claims presentes")
    void validateNullSubjectWithOtherClaims() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = Jwts.builder()
                .subject(null) // subject nulo
                .id("jti-null-subject")
                .claim("perfil", Perfil.SERVIDOR.name())
                .claim("unidade", 10L)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECURE_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty para token sem perfil")
    void validateMissingPerfil() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = Jwts.builder()
                .id("jti-sem-perfil")
                .subject("123")
                .claim("unidade", 1L)
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                // Missing perfil
                .signWith(Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty para token sem unidade")
    void validateMissingUnidade() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = Jwts.builder()
                .id("jti-sem-unidade")
                .subject("123")
                .claim("perfil", Perfil.ADMIN.name())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                // Missing unidade
                .signWith(Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve capturar exceção inesperada ao validar token")
    void validateUnexpectedException() {
        // Simular NPE ao acessar o segredo durante a validação
        when(jwtProperties.secret()).thenReturn(null);

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken("any.token");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve gerar e validar token de pré-autenticação")
    void preAuthSuccess() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = gerenciador.gerarTokenPreAuth("123");
        Optional<GerenciadorJwt.JwtPreAuthClaims> result = gerenciador.validarTokenPreAuth(token);

        assertThat(result).isPresent();
        assertThat(result.get().tituloEleitoral()).isEqualTo("123");
        assertThat(result.get().jti()).isNotBlank();
        assertThat(result.get().expiracao()).isAfter(java.time.Instant.now().minusSeconds(1));
    }

    @Test
    @DisplayName("Deve retornar empty para token pré-auth com tipo errado")
    void preAuthWrongType() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);
        when(jwtProperties.expiracaoMinutos()).thenReturn(60);

        // Token normal usado como pré-auth
        String token = gerenciador.gerarToken("123", Perfil.ADMIN, 1L);
        Optional<GerenciadorJwt.JwtPreAuthClaims> result = gerenciador.validarTokenPreAuth(token);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty para token pré-auth inválido")
    void preAuthInvalid() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);
        Optional<GerenciadorJwt.JwtPreAuthClaims> result = gerenciador.validarTokenPreAuth("invalid.token");
        assertThat(result).isEmpty();
    }

}
