package sgc.seguranca.login;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sgc.organizacao.UsuarioFacade;

import java.io.IOException;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("FiltroJwt Tests")
class FiltroJwtTest {

    @InjectMocks
    private FiltroJwt filtro;

    @Mock
    private GerenciadorJwt jwtService;
    @Mock
    private UsuarioFacade usuarioService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain filterChain;

    @Test
    @DisplayName("Deve ignorar requisição sem cabeçalho Authorization")
    void deveIgnorarSemAuthorization() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        filtro.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve ignorar cabeçalho Authorization que não seja Bearer")
    void deveIgnorarNaoBearer() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic 123");
        filtro.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Deve logar aviso quando usuário do JWT não é encontrado")
    void deveLogarAvisoUsuarioNaoEncontrado() throws ServletException, IOException {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);

        GerenciadorJwt.JwtClaims claims = new GerenciadorJwt.JwtClaims("123", null, null);
        when(jwtService.validarToken(token)).thenReturn(Optional.of(claims));

        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
