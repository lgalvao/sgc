package sgc.comum.erros;

import org.junit.jupiter.api.*;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RestExceptionHandler - Cobertura Adicional")
class RestExceptionHandlerCoverageTest {

    private final RestExceptionHandler handler = new RestExceptionHandler();

    @Test
    @DisplayName("handleErroInterno deve tratar ErroInterno corretamente")
    void deveTratarErroInterno() {

        ErroInterno ex = new ErroInterno("Messagem interna de teste") {
        };


        ResponseEntity<?> response = handler.handleErroInterno(ex);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErroApi body = (ErroApi) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).contains("ERRO INTERNO: Messagem interna de teste");
        assertThat(body.getCode()).isEqualTo("ERRO_INTERNO");
        assertThat(body.getTraceId()).isNotNull();
    }

    @Test
    @DisplayName("sanitizar deve lidar com texto nulo")
    void deveLidarComTextoNuloNoSanitizar() {

        class ErroTeste extends ErroNegocioBase {
            public ErroTeste() {
                super(null, "ERRO_TESTE", HttpStatus.BAD_REQUEST);
            }
        }
        ErroTeste ex = new ErroTeste();


        ResponseEntity<?> response = handler.handleErroNegocio(ex);


        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErroApi body = (ErroApi) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getMessage()).isEmpty(); // Deve retornar "" por causa do sanitizar(null)
    }
}
