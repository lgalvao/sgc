package sgc.seguranca.login;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import sgc.organizacao.OrganizacaoFacade;
import sgc.seguranca.LoginFacade;
import sgc.seguranca.dto.AutenticarRequest;

import static org.assertj.core.api.Assertions.assertThat;
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
        // Arrange
        AutenticarRequest request = new AutenticarRequest("111111", "senha_errada");
        when(loginFacade.autenticar("111111", "senha_errada")).thenReturn(false);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        // Act
        ResponseEntity<Boolean> result = controller.autenticar(request, httpRequest, httpResponse);

        // Assert
        assertThat(result.getBody()).isFalse();
        verify(httpResponse, never()).addCookie(any()); // Cobertura da linha 64 (branch false)
    }
}
