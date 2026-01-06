package sgc.seguranca;

import org.junit.jupiter.api.DisplayName;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        when(jwtProperties.getSecret()).thenReturn(SECURE_SECRET);
        assertThatCode(() -> gerenciador.verificarSegurancaChave())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve alertar (warn) se segredo padrão em ambiente de teste")
    void warnDefaultTest() {
        when(jwtProperties.getSecret()).thenReturn(DEFAULT_SECRET);
        when(environment.acceptsProfiles(Profiles.of("test", "e2e", "local"))).thenReturn(true);
        assertThatCode(() -> gerenciador.verificarSegurancaChave())
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve falhar se segredo padrão em produção")
    void failDefaultProd() {
        when(jwtProperties.getSecret()).thenReturn(DEFAULT_SECRET);
        when(environment.acceptsProfiles(Profiles.of("test", "e2e", "local"))).thenReturn(false);
        
        assertThatThrownBy(() -> gerenciador.verificarSegurancaChave())
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("FALHA DE SEGURANÇA");
    }

    @Test
    @DisplayName("Deve falhar se segredo muito curto")
    void failShortSecret() {
        // Mock getSecret calls in verification and generation
        when(jwtProperties.getSecret()).thenReturn(DEFAULT_SECRET_SHORT);
        
        // init check doesn't check length, only equality to default.
        // length check happens on signing key generation which happens on usage.
        
        assertThatThrownBy(() -> gerenciador.gerarToken("123", Perfil.ADMIN, 1L))
                .isInstanceOf(ErroConfiguracao.class)
                .hasMessageContaining("mínimo 32 caracteres");
    }

    @Test
    @DisplayName("Deve validar token com sucesso")
    void validateSuccess() {
        when(jwtProperties.getSecret()).thenReturn(SECURE_SECRET);
        when(jwtProperties.getExpiracaoMinutos()).thenReturn(60);

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
        when(jwtProperties.getSecret()).thenReturn(SECURE_SECRET);
        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken("invalid.token.here");
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("Deve retornar empty para token com claims faltando (simulado)")
    void validateMissingClaims() {
        when(jwtProperties.getSecret()).thenReturn(SECURE_SECRET);
        
        // Gerar token "mal formado" (business logic wise) mas valido (crypto wise)
        String token = io.jsonwebtoken.Jwts.builder()
                .subject(null) // missing subject
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(SECURE_SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .compact();

        Optional<GerenciadorJwt.JwtClaims> result = gerenciador.validarToken(token);
        assertThat(result).isEmpty(); // Deve falhar no null check dos claims
    }
}
