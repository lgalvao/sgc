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
import sgc.seguranca.config.JwtProperties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@Tag("security")
@DisplayName("GerenciadorJwt - Segurança")
class GerenciadorJwtSecurityTest {

    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-32-chars";
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private Environment environment;
    @InjectMocks
    private GerenciadorJwt gerenciadorJwt;

    @Test
    @DisplayName("Deve lançar erro crítico se usar secret padrão em ambiente produtivo")
    void deveLancarErroEmProducaoComSecretPadrao() {
        // Given
        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET);
        // Simula que NÃO é um ambiente de teste/dev (retorna false para os perfis
        // seguros)
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        // When & Then
        assertThrows(ErroConfiguracao.class, () -> gerenciadorJwt.verificarSegurancaChave());
    }
}
