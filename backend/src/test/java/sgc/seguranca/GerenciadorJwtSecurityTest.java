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
import sgc.seguranca.config.JwtProperties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GerenciadorJwt - Segurança")
class GerenciadorJwtSecurityTest {

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private Environment environment;

    @InjectMocks
    private GerenciadorJwt gerenciadorJwt;

    private static final String DEFAULT_SECRET = "sgc-secret-key-change-this-in-production-minimum-32-chars";

    @Test
    @DisplayName("Deve lançar erro crítico se usar secret padrão em ambiente produtivo")
    void deveLancarErroEmProducaoComSecretPadrao() {
        // Given
        when(jwtProperties.getSecret()).thenReturn(DEFAULT_SECRET);
        // Simula que NÃO é um ambiente de teste/dev (retorna false para os perfis seguros)
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        // When & Then
        assertThrows(ErroConfiguracao.class, () -> gerenciadorJwt.verificarSegurancaChave());
    }
}
