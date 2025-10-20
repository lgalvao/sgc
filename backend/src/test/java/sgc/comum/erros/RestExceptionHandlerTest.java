package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class RestExceptionHandlerTest {

    @InjectMocks
    private RestExceptionHandler handler;

    @Test
    @DisplayName("Deve tratar ErroDominioNaoEncontrado com status 404")
    void handleErroDominioNaoEncontrado() {
        ErroDominioNaoEncontrado ex = new ErroDominioNaoEncontrado("Teste");
        ResponseEntity<Object> response = handler.handleErroDominioNaoEncontrado(ex);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar ErroDominioAccessoNegado com status 403")
    void handleErroDominioAccessoNegado() {
        ErroDominioAccessoNegado ex = new ErroDominioAccessoNegado("Teste");
        ResponseEntity<Object> response = handler.handleErroDominioAccessoNegado(ex);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException com status 409")
    void handleIllegalStateException() {
        IllegalStateException ex = new IllegalStateException("Teste");
        ResponseEntity<Object> response = handler.handleIllegalStateException(ex);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com status 500")
    void handleGenericException() {
        Exception ex = new Exception("Teste");
        ResponseEntity<Object> response = handler.handleGenericException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
