package sgc.seguranca.login;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.mockito.junit.jupiter.*;
import sgc.organizacao.*;

import java.io.*;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

        GerenciadorJwt.JwtClaims claims = new GerenciadorJwt.JwtClaims("12345", null, null);
        when(jwtService.validarToken(token)).thenReturn(Optional.of(claims));

        when(usuarioService.carregarUsuarioParaAutenticacao("12345")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve processar JWT via Cookie")
    void deveProcessarJwtViaCookie() throws ServletException, IOException {
        Cookie cookie = new Cookie("jwtToken", "token-cookie");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        
        filtro.doFilterInternal(request, response, filterChain);
        
        verify(jwtService).validarToken("token-cookie");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve lidar com cookies nulos")
    void deveLidarComCookiesNulos() throws ServletException, IOException {
        when(request.getCookies()).thenReturn(null);
        when(request.getHeader("Authorization")).thenReturn(null);
        
        filtro.doFilterInternal(request, response, filterChain);
        
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Deve cobrir ramificações do método mascarar")
    void deveCobrirMascarar() throws ServletException, IOException {
        // Para testar o mascarar(valor) precisamos que caia no log.warn
        // Caso valor.length <= 4
        String token = "short-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validarToken(token)).thenReturn(Optional.of(new GerenciadorJwt.JwtClaims("123", null, null)));
        when(usuarioService.carregarUsuarioParaAutenticacao("123")).thenReturn(null);

        filtro.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
    }
}
