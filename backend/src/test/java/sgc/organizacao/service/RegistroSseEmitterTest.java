package sgc.organizacao.service;

import org.junit.jupiter.api.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

import static org.assertj.core.api.Assertions.*;

class RegistroSseEmitterTest {

    private RegistroSseEmitter registroSseEmitter;

    @BeforeEach
    void setUp() {
        registroSseEmitter = new RegistroSseEmitter();
    }

    @Test
    @DisplayName("deve retornar emissor válido ao registrar")
    void deveRetornarEmissorValido() {
        SseEmitter emitter = registroSseEmitter.registrar();

        assertThat(emitter).isNotNull();
    }

    @Test
    @DisplayName("deve transmitir evento sem erro para emissores registrados")
    void deveTransmitirEventoSemErro() {
        registroSseEmitter.registrar();

        assertThatCode(() -> registroSseEmitter.transmitir("meu-evento"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("deve transmitir evento sem erro quando não há emissores registrados")
    void deveTransmitirSemEmissores() {
        assertThatCode(() -> registroSseEmitter.transmitir("meu-evento"))
                .doesNotThrowAnyException();
    }
}
