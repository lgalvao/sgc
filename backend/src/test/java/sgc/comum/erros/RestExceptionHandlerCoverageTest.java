package sgc.comum.erros;

import jakarta.servlet.http.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.junit.jupiter.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.web.context.request.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestExceptionHandler - Cobertura de Testes")
class RestExceptionHandlerCoverageTest {

    private RestExceptionHandler target;

    @BeforeEach
    void setUp() {
        target = new RestExceptionHandler();
    }

    private HttpMessageNotWritableException createEx() {
        return new HttpMessageNotWritableException("Erro de escrita");
    }

    @Test
    @DisplayName("Deve cobrir descreverRequisicao quando não for ServletWebRequest")
    void deveCobrirDescreverRequisicaoNaoServlet() {
        WebRequest mockRequest = mock(WebRequest.class);
        
        ResponseEntity<Object> response = target.handleHttpMessageNotWritable(
                createEx(), null, HttpStatus.INTERNAL_SERVER_ERROR, mockRequest);
                
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve cobrir descreverRequisicao quando for ServletWebRequest")
    void deveCobrirDescreverRequisicaoServlet() {
        ServletWebRequest mockRequest = mock(ServletWebRequest.class);
        HttpServletRequest mockHttpRequest = mock(HttpServletRequest.class);
        
        when(mockRequest.getRequest()).thenReturn(mockHttpRequest);
        when(mockHttpRequest.getMethod()).thenReturn("POST");
        when(mockHttpRequest.getRequestURI()).thenReturn("/api/teste");
        
        target.handleHttpMessageNotWritable(createEx(), null, HttpStatus.INTERNAL_SERVER_ERROR, mockRequest);
        
        verify(mockRequest).getRequest();
    }

    @Test
    @DisplayName("Deve cobrir obterCausaRaiz com múltiplas causas")
    void deveCobrirObterCausaRaizMultiplasCausas() {
        Exception causaRaiz = new RuntimeException("Raiz");
        Exception causaMeio = new RuntimeException("Meio", causaRaiz);
        Exception ex = new RuntimeException("Topo", causaMeio);
        WebRequest request = mock(WebRequest.class);
        
        ResponseEntity<Object> response = target.handleHttpMessageNotWritable(
                new HttpMessageNotWritableException("Erro", ex), 
                null, HttpStatus.INTERNAL_SERVER_ERROR, request);
                
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("Deve cobrir handleErroNegocio com erro 4xx e detalhes")
    @SuppressWarnings("unchecked")
    void deveCobrirHandleErroNegocio4xxComDetalhes() {
        Map<String, String> details = new HashMap<>();
        details.put("campo", "erro no campo");
        
        ErroNegocioBase ex = new ErroNegocioBase("Mensagem Erro", "COD_400", HttpStatus.BAD_REQUEST, details) {};
        
        ResponseEntity<ErroApi> response = target.handleErroNegocio(ex);
        ErroApi corpo = Objects.requireNonNull(response.getBody());
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(corpo.getMessage()).isEqualTo("Mensagem Erro");
        
        Map<String, Object> responseDetails = (Map<String, Object>) corpo.getDetails();
        assertThat(responseDetails).containsEntry("campo", "erro no campo");
    }

    @Test
    @DisplayName("Deve cobrir handleErroNegocio com erro 5xx")
    void deveCobrirHandleErroNegocio5xx() {
        ErroNegocioBase ex = new ErroNegocioBase("Erro Crítico", "COD_500", HttpStatus.INTERNAL_SERVER_ERROR) {};
        
        ResponseEntity<ErroApi> response = target.handleErroNegocio(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    @DisplayName("Deve cobrir handleGenericException com mensagem nula")
    void deveCobrirHandleGenericExceptionMensagemNula() {
        Exception ex = new NullPointerException(); 
        
        ResponseEntity<ErroApi> response = target.handleGenericException(ex);
        ErroApi corpo = Objects.requireNonNull(response.getBody());
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(corpo.getMessage()).contains("NullPointerException");
    }
}
