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
@DisplayName("Testes do RestExceptionHandler")
class RestExceptionHandlerTest {

    @InjectMocks
    private RestExceptionHandler handler;

    @Test
    @DisplayName("Deve tratar ErroEntidadeNaoEncontrada com status 404")
    void deveTratarErroDominioNaoEncontrado() {
        // Arrange
        ErroEntidadeNaoEncontrada ex = new ErroEntidadeNaoEncontrada("Teste");

        // Act
        ResponseEntity<Object> response = handler.handleErroNegocio(ex);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar ErroAccessoNegado com status 403")
    void deveTratarErroAccessoNegado() {
        // Arrange
        ErroAccessoNegado ex = new ErroAccessoNegado("Teste");

        // Act
        ResponseEntity<Object> response = handler.handleErroNegocio(ex);

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar ErroRequisicaoSemCorpo com status 400")
    void deveTratarErroRequisicaoSemCorpo() {
        // Arrange
        ErroRequisicaoSemCorpo ex = new ErroRequisicaoSemCorpo("Teste");

        // Act
        ResponseEntity<Object> response = handler.handleErroNegocio(ex);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar IllegalStateException com status 409")
    void deveTratarIllegalStateException() {
        // Arrange
        IllegalStateException ex = new IllegalStateException("Teste");

        // Act
        ResponseEntity<Object> response = handler.handleIllegalStateException(ex);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar Exception genérica com status 500")
    void deveTratarGenericException() {
        // Arrange
        Exception ex = new Exception("Teste");

        // Act
        ResponseEntity<Object> response = handler.handleGenericException(ex);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
