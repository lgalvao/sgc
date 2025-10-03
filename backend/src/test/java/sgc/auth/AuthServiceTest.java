package sgc.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import sgc.dto.LoginRequest;
import sgc.dto.LoginResponse;
import sgc.dto.PerfilUnidadeDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private SistemaAcessoClient sistemaAcessoClient;

    @Mock
    private TokenService tokenService;

    @Mock
    private org.springframework.context.ApplicationEventPublisher publisher;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_publishes_event_and_returns_response() {
        LoginRequest req = new LoginRequest("validUser", "senha");
        List<PerfilUnidadeDTO> perfis = List.of(new PerfilUnidadeDTO("CHEFE", 1L, "SESEL"));

        when(sistemaAcessoClient.authenticate("validUser", "senha")).thenReturn(true);
        when(sistemaAcessoClient.fetchPerfis("validUser")).thenReturn(perfis);
        when(tokenService.generateToken("validUser")).thenReturn("tok123");

        LoginResponse resp = authService.login(req);

        assertNotNull(resp);
        assertEquals("tok123", resp.getToken());
        assertEquals(perfis, resp.getPerfis());
        assertEquals(perfis, resp.getUnidades());

        verify(publisher, times(1)).publishEvent(any());
    }

    @Test
    void login_invalid_credentials_throws_401() {
        LoginRequest req = new LoginRequest("badUser", "wrong");

        when(sistemaAcessoClient.authenticate("badUser", "wrong")).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(req));
        assertEquals(401, ex.getStatusCode().value());
        assertTrue(ex.getReason().contains("Título ou senha inválidos"));

        verify(publisher, never()).publishEvent(any());
    }

    @Test
    void login_null_request_throws_bad_request() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> authService.login(null));
        assertEquals(400, ex.getStatusCode().value());
    }
}