package sgc.comum.erros;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestExceptionHandler - Cobertura de Testes")
class RestExceptionHandlerCoverageTest {

    @InjectMocks
    private RestExceptionHandler target;

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
        
        target.handleHttpMessageNotWritable(
                new HttpMessageNotWritableException("Erro", ex), 
                null, HttpStatus.INTERNAL_SERVER_ERROR, null);
    }

    @Test
    @DisplayName("Deve cobrir handleErroNegocio com erro 4xx e detalhes")
    @SuppressWarnings("unchecked")
    void deveCobrirHandleErroNegocio4xxComDetalhes() {
        Map<String, String> details = new HashMap<>();
        details.put("campo", "erro no campo");
        
        ErroNegocioBase ex = new ErroNegocioBase("Mensagem Erro", "COD_400", HttpStatus.BAD_REQUEST, details) {};
        
        ResponseEntity<ErroApi> response = target.handleErroNegocio(ex);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getMessage()).isEqualTo("Mensagem Erro");
        
        Map<String, Object> responseDetails = (Map<String, Object>) response.getBody().getDetails();
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
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getMessage()).contains("NullPointerException");
    }
}
