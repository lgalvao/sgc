package sgc.seguranca.login;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.core.env.*;
import sgc.comum.erros.*;
import sgc.seguranca.config.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        when(jwtProperties.secret()).thenReturn(DEFAULT_SECRET);
        // Simula que NÃO é um ambiente de teste/dev (retorna false para os perfis
        // seguros)
        when(environment.acceptsProfiles(any(Profiles.class))).thenReturn(false);

        // When & Then
        assertThrows(ErroConfiguracao.class, () -> gerenciadorJwt.verificarSegurancaChave());
    }
}
