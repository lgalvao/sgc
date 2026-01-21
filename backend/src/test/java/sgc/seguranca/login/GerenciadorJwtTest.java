package sgc.seguranca.login;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import sgc.comum.erros.ErroConfiguracao;
import sgc.organizacao.model.Perfil;
import sgc.seguranca.config.JwtProperties;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("GerenciadorJwt - Testes Unitários")
class GerenciadorJwtTest {

    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private Environment environment;

    @InjectMocks
    private GerenciadorJwt gerenciador;

    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-32-chars";
    private static final String DEFAULT_SECRET_SHORT = "short";
    private static final String SECURE_SECRET = "secure-secret-key-minimum-32-chars-length-xyz-123";

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
    @DisplayName("Deve falhar se segredo muito curto")
    void failShortSecret() {
        // Mock secret() calls in verification and generation
        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET_SHORT);

        // init check doesn't check length, only equality to default.
        // length check happens on signing key generation which happens on usage.

        assertThatThrownBy(() -> gerenciador.gerarToken("123", Perfil.ADMIN, 1L))
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
        String token = io.jsonwebtoken.Jwts.builder()
                .subject(null) // missing subject
                .signWith(io.jsonwebtoken.security.Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty(); // Deve falhar no null check dos claims
    }

    @Test
    @DisplayName("Deve retornar empty para token sem perfil")
    void validateMissingPerfil() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = io.jsonwebtoken.Jwts.builder()
                .subject("123")
                .claim("unidade", 1L)
                // Missing perfil
                .signWith(io.jsonwebtoken.security.Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar empty para token sem unidade")
    void validateMissingUnidade() {
        when(jwtProperties.secret()).thenReturn(SECURE_SECRET);

        String token = io.jsonwebtoken.Jwts.builder()
                .subject("123")
                .claim("perfil", Perfil.ADMIN.name())
                // Missing unidade
                .signWith(io.jsonwebtoken.security.Keys
                        .hmacShaKeyFor(SECURE_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve capturar exceção inesperada ao validar token")
    void validateUnexpectedException() {
        // Simular NPE ao acessar o segredo durante a validação
        when(jwtProperties.secret()).thenReturn(null);

        // O token pode ser qualquer string, pois o erro vai ocorrer ao tentar obter a chave (antes do parse)
        // ou durante o parse se o mock for chamado lá.
        // O método validarToken chama getSigningKey() -> jwtProperties.secret().

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken("any.token");

        assertThat(result).isEmpty();
    }
}
