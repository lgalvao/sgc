package sgc.seguranca.login;

import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import org.springframework.http.*;
import sgc.organizacao.*;
import sgc.seguranca.*;
import sgc.seguranca.dto.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginController - Cobertura Adicional")
class LoginControllerCoverageTest {

    @Mock
    private LoginFacade loginFacade;
    @Mock
    private OrganizacaoFacade organizacaoFacade;
    @Mock
    private LimitadorTentativasLogin limitadorTentativasLogin;
    @Mock
    private GerenciadorJwt gerenciadorJwt;
    @Mock
    private HttpServletRequest httpRequest;
    @Mock
    private HttpServletResponse httpResponse;

    @InjectMocks
    private LoginController controller;

    @Test
    @DisplayName("autenticar deve retornar false e não gerar cookie se falhar")
    void deveRetornarFalseSeFalharAutenticacao() {

        AutenticarRequest request = new AutenticarRequest("111111", "senha_errada");
        when(loginFacade.autenticar("111111", "senha_errada")).thenReturn(false);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");


        ResponseEntity<Boolean> result = controller.autenticar(request, httpRequest, httpResponse);


        assertThat(result.getBody()).isFalse();
        verify(httpResponse, never()).addCookie(any()); // Cobertura da linha 64 (branch false)
    }
}
