package sgc.comum.erros;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@DisplayName("RestExceptionHandler - Cobertura Adicional")
class RestExceptionHandlerCoverageTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("handleErroInterno deve tratar ErroInterno corretamente")
    void deveTratarErroInterno() {
        // Arrange
        ErroInterno ex = new ErroInterno("Messagem interna de teste") {};

        // Act
        ResponseEntity<?> response = handler.handleErroInterno(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErroApi body = (ErroApi) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("Erro interno do sistema");
        assertThat(body.getCode()).isEqualTo("ERRO_INTERNO");
        assertThat(body.getTraceId()).isNotNull();
    }

    @Test
    @DisplayName("sanitizar deve lidar com texto nulo")
    void deveLidarComTextoNuloNoSanitizar() {
        // Arrange
        class ErroTeste extends ErroNegocioBase {
            public ErroTeste() {
                super(null, "ERRO_TESTE", HttpStatus.BAD_REQUEST);
            }
        }
        ErroTeste ex = new ErroTeste();

        // Act
        ResponseEntity<?> response = handler.handleErroNegocio(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErroApi body = (ErroApi) response.getBody();
        assertThat(body.getMessage()).isEmpty(); // Deve retornar "" por causa do sanitizar(null)
    }
}
