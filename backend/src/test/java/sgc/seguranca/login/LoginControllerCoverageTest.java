package sgc.seguranca.login;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import sgc.comum.erros.ErroAutenticacao;
import sgc.organizacao.UsuarioFacade;
import sgc.seguranca.login.dto.AutenticarRequest;
import sgc.seguranca.login.dto.EntrarRequest;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerCoverageTest {

    @InjectMocks
    private LoginController controller;

    @Mock
    private LoginFacade loginFacade;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private LimitadorTentativasLogin limitadorTentativasLogin;
    @Mock
    private GerenciadorJwt gerenciadorJwt;

    @Test
    @DisplayName("autenticar deve configurar cookie seguro se ambienteTestes false")
    void autenticar_SecureCookie() {
        ReflectionTestUtils.setField(controller, "ambienteTestes", false);
        AutenticarRequest req = new AutenticarRequest("user", "pass");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        when(loginFacade.autenticar("user", "pass")).thenReturn(true);
        when(gerenciadorJwt.gerarTokenPreAuth("user")).thenReturn("token");

        controller.autenticar(req, httpReq, httpRes);

        verify(httpRes).addCookie(argThat(Cookie::getSecure));
    }

    @Test
    @DisplayName("autenticar NAO deve configurar cookie seguro se ambienteTestes true")
    void autenticar_NonSecureCookie() {
        ReflectionTestUtils.setField(controller, "ambienteTestes", true);
        AutenticarRequest req = new AutenticarRequest("user", "pass");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        when(loginFacade.autenticar("user", "pass")).thenReturn(true);
        when(gerenciadorJwt.gerarTokenPreAuth("user")).thenReturn("token");

        controller.autenticar(req, httpReq, httpRes);

        verify(httpRes).addCookie(argThat(cookie -> !cookie.getSecure()));
    }

    @Test
    @DisplayName("extrairIp deve retornar null se headers e remoteAddr nulos")
    void extrairIp_ReturnsNull() {
        // Need to call a public method that calls extrairIp.
        // autenticar calls extrairIp.
        AutenticarRequest req = new AutenticarRequest("user", "pass");
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        HttpServletResponse httpRes = mock(HttpServletResponse.class);

        when(httpReq.getHeader("X-Forwarded-For")).thenReturn(null);
        when(httpReq.getRemoteAddr()).thenReturn(null);
        
        // This will trigger extrairIp -> null.
        // limitadorTentativasLogin.verificar(null) check?
        // Method signature is verify(String ip). Does it fail on null?
        // Assuming mock accepts null.

        controller.autenticar(req, httpReq, httpRes);

        verify(limitadorTentativasLogin).verificar(null);
    }
    
    @Test
    @DisplayName("entrar deve lidar com cookie invalido e lancar ErroAutenticacao")
    void entrar_CookieInvalido() {
        EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        
        // No cookies
        when(httpReq.getCookies()).thenReturn(null);

        assertThrows(ErroAutenticacao.class, () -> controller.entrar(req, httpReq));
    }

    @Test
    @DisplayName("entrar deve lidar com sessao invalida (token mismatch)")
    void entrar_TokenMismatch() {
        EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("SGC_PRE_AUTH", "token");
        
        when(httpReq.getCookies()).thenReturn(new Cookie[]{cookie});
        when(gerenciadorJwt.validarTokenPreAuth("token")).thenReturn(Optional.of("otherUser"));

        assertThrows(ErroAutenticacao.class, () -> controller.entrar(req, httpReq));
    }
    
    @Test
    @DisplayName("entrar deve lidar com sessao invalida (token empty)")
    void entrar_TokenEmpty() {
        EntrarRequest req = new EntrarRequest("user", "ADMIN", 1L);
        HttpServletRequest httpReq = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("SGC_PRE_AUTH", "token");
        
        when(httpReq.getCookies()).thenReturn(new Cookie[]{cookie});
        when(gerenciadorJwt.validarTokenPreAuth("token")).thenReturn(Optional.empty());

        assertThrows(ErroAutenticacao.class, () -> controller.entrar(req, httpReq));
    }
}
